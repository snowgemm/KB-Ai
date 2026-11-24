use jni::EnvUnowned;
use jni::errors::ThrowRuntimeExAndDefault;
use jni::objects::{JObject, JString};

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_snowgem_kbai_NativeBridge_nativeStatus<'local>(
    mut env: EnvUnowned<'local>,
    _this: JObject<'local>,
) -> JString<'local> {
    env.with_env(|env| env.new_string(kb_core::native_status()))
        .resolve::<ThrowRuntimeExAndDefault>()
}
