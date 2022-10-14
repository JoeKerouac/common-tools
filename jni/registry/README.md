# 构建说明
## 项目创建、构建
使用vs2017创建动态链接库项目，将 `RegistryKey.c` 、 `RegistryKey.h` 、 `RegistryKeyProto.h` 三个文件加入到项目的源文件，然后点击生成来
生成对应平台（x86、x64）的dll文件；

## 构建常见错误
### 错误一

```
C4996	'sprintf': This function or variable may be unsafe. Consider using sprintf_s instead. To disable deprecation, use _CRT_SECURE_NO_WARNINGS. See online help for details.	registry
```

出现以上错误，需要打开项目属性页->配置属性->C/C++->预处理器，编辑预处理器定义，添加 `_CRT_SECURE_NO_WARNINGS` 预处理器；

### 错误二

```
C1010	在查找预编译头时遇到意外的文件结尾。是否忘记了向源中添加“#include "pch.h"”?
```

出现以上错误，需要打开项目属性页->配置属性->C/C++->预编译头，选择不使用预编译头；

### 错误三

```
C4703	使用了可能未初始化的本地指针变量“dataPtr”
```

出现以上错误，需要打开项目属性页->配置属性->C/C++->常规，设置 `SDL检查` 为否；


### 错误四
```
无法打开源文件：<jni.h>
```

没有配置include，打开项目属性页->C/C++->常规->附加包含目录->编辑，将Java的include文件夹添加到这里；


## jni调试
debug运行Java项目，在进入native代码前断点，然后在vs中打开jni代码，选择调试->附加到进程，选择Java进程，然后放开Java中的断点，等到执行到c代码
的时候就可以在vs中调试了；