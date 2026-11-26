use std::collections::HashMap;
use std::sync::atomic::{AtomicI64, Ordering};
use std::sync::{Mutex, MutexGuard, OnceLock};

use jni::EnvUnowned;
use jni::errors::ThrowRuntimeExAndDefault;
use jni::objects::{JObject, JString};
use jni::sys::{jboolean, jint, jlong};

use kb_core::InputSession;

static NEXT_SESSION_ID: AtomicI64 = AtomicI64::new(1);
static SESSIONS: OnceLock<Mutex<HashMap<i64, InputSession>>> = OnceLock::new();

fn session_store() -> &'static Mutex<HashMap<i64, InputSession>> {
    SESSIONS.get_or_init(|| Mutex::new(HashMap::new()))
}

fn lock_sessions() -> MutexGuard<'static, HashMap<i64, InputSession>> {
    session_store()
        .lock()
        .unwrap_or_else(std::sync::PoisonError::into_inner)
}

fn with_session<T>(handle: jlong, action: impl FnOnce(&mut InputSession) -> T) -> Option<T> {
    lock_sessions().get_mut(&handle).map(action)
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_snowgem_kbai_NativeBridge_nativeStatus<'local>(
    mut env: EnvUnowned<'local>,
    _this: JObject<'local>,
) -> JString<'local> {
    env.with_env(|env| env.new_string(kb_core::native_status()))
        .resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_snowgem_kbai_NativeBridge_nativeCreateSession<'local>(
    mut env: EnvUnowned<'local>,
    _this: JObject<'local>,
) -> jlong {
    env.with_env(|_| -> jni::errors::Result<jlong> {
        let handle = NEXT_SESSION_ID.fetch_add(1, Ordering::Relaxed);
        if handle <= 0 {
            return Ok(0);
        }

        let session = InputSession::new();
        lock_sessions().insert(handle, session);
        Ok(handle)
    })
    .resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_snowgem_kbai_NativeBridge_nativeProcessKey<'local>(
    mut env: EnvUnowned<'local>,
    _this: JObject<'local>,
    handle: jlong,
    key_code: jint,
    vietnamese: jboolean,
) -> JString<'local> {
    env.with_env(|env| {
        let mut sessions = lock_sessions();
        let preedit = sessions
            .get_mut(&handle)
            .and_then(|session| {
                char::from_u32(key_code as u32).map(|key| session.process_key(key, vietnamese))
            })
            .unwrap_or("");
        env.new_string(preedit)
    })
    .resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_snowgem_kbai_NativeBridge_nativeWasForcedCommit<'local>(
    _env: EnvUnowned<'local>,
    _this: JObject<'local>,
    handle: jlong,
) -> jboolean {
    with_session(handle, |session| session.was_forced_commit()).unwrap_or(false)
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_snowgem_kbai_NativeBridge_nativeUndoKey<'local>(
    mut env: EnvUnowned<'local>,
    _this: JObject<'local>,
    handle: jlong,
) -> JString<'local> {
    env.with_env(|env| {
        let mut sessions = lock_sessions();
        let preedit = sessions
            .get_mut(&handle)
            .and_then(|session| session.undo_last_key())
            .unwrap_or("");
        env.new_string(preedit)
    })
    .resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_snowgem_kbai_NativeBridge_nativeCommit<'local>(
    _env: EnvUnowned<'local>,
    _this: JObject<'local>,
    handle: jlong,
) {
    let _ = with_session(handle, |session| session.commit());
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_snowgem_kbai_NativeBridge_nativeDestroySession<'local>(
    _env: EnvUnowned<'local>,
    _this: JObject<'local>,
    handle: jlong,
) {
    lock_sessions().remove(&handle);
}
