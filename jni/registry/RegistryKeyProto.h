

LONG
maxSubKeyLength
		( HKEY hKey, DWORD *pdwSubkeyLen );

LONG
maxValueNameLength
		( HKEY hKey, DWORD *pdwNameLen );

DWORD
getDWordValueData
		( JNIEnv *env, HKEY hKey, char *valueName );

jstring
getStringValueData
		( JNIEnv *env, HKEY hKey, char *valueName );

jarray
getMultiStringValueData
		( JNIEnv *env, HKEY hKey, char *valueName );

jarray
getBinaryValueData
		( JNIEnv *env, HKEY hKey, char *valueName );

void
setKeyValue
		( JNIEnv *env, HKEY hKey, char *valueName,
			DWORD dwType, jobject valObject );

jstring
buildFullKeyName
		( JNIEnv *env, jobject rkObject, char *subKeyName );

jstring
asciiToJString
		( JNIEnv *env, char *asciiStr );

jstring
strbufToJString
		( JNIEnv *env, char *buf, int len );

void
jStringToAscii
		( JNIEnv *env, jstring jStr, char *asciiBuf, int asciiLen );

char *
jStringToNewAscii
		( JNIEnv *env, jstring jStr );

jobject
newRegistryValue
		( JNIEnv *env, jobject rkObject,
			jstring valueName, char *className, int type );

jobject
newRegistryKey
		( JNIEnv *env, HKEY hKey, jstring name, BOOL created );

HKEY
getRegistryKey
		( JNIEnv *env, jobject rkObject );

DWORD
getRegistryValueType
		( JNIEnv *env, jobject valueObject );

jstring
getRegistryKeyName( JNIEnv *env, jobject rkObject );

BOOL
RegIsHKeyRemote
		( HKEY hKey );

BOOL
isThrowing
		( JNIEnv *env );

jint
throwNoSuchKeyException
		( JNIEnv *env, char *message );

jint
throwNoSuchValueException
		( JNIEnv *env, char *message );

jint
throwNoClassDefError
		( JNIEnv *env, char *message );

jint
throwNoSuchMethodError
		( JNIEnv *env, char *className,
			char *methodName, char *methodSignature );

jint
throwNoSuchFieldError
		( JNIEnv *env, char *message );

jint
throwOutOfMemoryError
		( JNIEnv *env, char *message );

void
throwSpecificRegError
		( JNIEnv *env, LONG regErr, char *message,
			char *keyName, char *valueName );

jint
throwRegErrorException
		( JNIEnv *env, LONG regErr, char *message );

jobject
newInteger(JNIEnv *env, jint value);

jint
getIntValue(JNIEnv *env, jobject integer);
