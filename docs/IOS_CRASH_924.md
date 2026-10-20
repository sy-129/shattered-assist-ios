# 4.0.2-assist.4（925）：修复 iOS 运行库兼容性

## 用户报告

- 预期：打开应用后可以开始地牢游戏，也可以进入独立家园。
- 实际：主菜单能够打开，开始游戏后闪退；用户也报告家园入口闪退。
- 位置：iPhone 16 Pro，用户报告 iOS 27.0.1，轻松签安装；安装版本号尚未由用户确认。
- 复现：启动应用→开始游戏→进入地牢；或启动应用→独立家园。
- 尚缺证据：设备上的 Exception Type、Termination Reason 与崩溃线程，暂不能排除其他故障。

## 已确认的代码缺陷及修复

直接检查实际依赖的 `robovm-rt-2.3.24.jar`，确认它没有 `java.util.function.BooleanSupplier`、`Float.isFinite`、`Math.floorMod`、`String.join`。电脑JDK具备这些API，所以原桌面测试与编译未发现它们在iOS上不可用。

- `ui/TouchJoystick`：用项目自己的布尔回调接口替代缺失的BooleanSupplier，两种场景都会创建该摇杆。
- `assist/TouchStickState`：有限数检查改用运行库具备的isNaN/isInfinite，八方向编号用等价取余；此前辅助位置读取及家园相机计算也会调用有限数检查。
- `home/HomeWorld`：随机索引使用非负余数替代floorMod，家园事件文本使用StringBuilder替代join。
- `tools/verify_ios_runtime.py`与`:ios:verifyRuntimeApi`：直接解析编译字节码，对照真实RoboVM运行库检查Java成员引用，覆盖继承解析，并纳入每次云构建。排除由RoboVM AOT转换的invoke引导代码。

修复后本地25项逻辑测试及345项Java API成员引用检查通过。新IPA由macOS重新编译；成功工作流与具体校验值见[构建信息](../packages/build-info.json)及仓库Actions。上述结论证明已移除这些不兼容调用，仍需在用户设备安装925版复验闪退是否消失。

## 安装和下载

在Releases附件下载 `ShatteredAssist.ipa`，使用轻松签自己的有效证书及描述文件重签。保留相同包名覆盖安装，以尽量保留现有存档；不要先删除旧应用。

Packages中的OCI包包含IPA、源码ZIP、校验文件和许可证，是文件分发包；手机安装请选择Releases中的IPA。

原游戏、家园、婚姻养育、三种动物生态、骑乘、中文摇杆、辅助按钮与用户图标保留。原作者及外部素材鸣谢见README和LICENSE。
