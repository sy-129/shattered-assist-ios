# Shattered Assist · 地牢辅助版

基于 [Shattered Pixel Dungeon](https://github.com/00-Evan/shattered-pixel-dungeon) 4.0.2 的个人修改版，保留完整游戏和原作者署名，按 GPL-3.0-or-later 提供对应源码。不是原作者的官方发行版。

## 安装

构建成功后下载 [ShatteredAssist.ipa](packages/ShatteredAssist.ipa?raw=true)，导入轻松签，使用你自己的有效证书及描述文件签名后安装。无需钥匙串授权码、巨魔、越狱或跨进程权限。证书有效性、设备登记、开发者模式等仍遵循你的签名方式要求。

目标设备：iPhone 16 Pro / iOS 27.0.1。云端编译成功不代表已在该设备实测，实际状态见 packages/build-info.json。

## 游戏内辅助

进入地牢，等到角色可操作时点击左侧“辅助”按钮；长按可切换到另一侧。菜单只存在于本游戏内。

- 武器／徒手攻击倍率：1、2、5、10倍循环；倍率应用在伤害随机值计算后、敌人防御结算前，不包含法杖等独立伤害来源。
- 阻止生命伤害：阻止进入 Hero.damage 的正伤害，不承诺覆盖脚本直接死亡、剧情结算或生命上限变化。
- 免饥饿：清空饥饿累计并停止饥饿增长，关闭后恢复原有饥饿规则。
- 一键恢复生命：只对仍存活且当前可操作的角色恢复生命，不复活死者或消除异常状态。
- 关闭全部辅助：恢复1倍伤害，关闭免伤、免饥饿。

默认关闭，选项持久保存并影响后续对局。独立 bundle ID 与官方版区分；仍建议备份个人存档，不要修改为官方包名覆盖安装。

## 构建与验证

macOS + Xcode + JDK21：

```sh
./gradlew -PiosOnly :core:test
./gradlew -PiosOnly -Probovm.archs=arm64 -Probovm.iosSkipSigning=true :ios:createIPA
python3 tools/package_ios.py
```

GitHub Actions 自动执行上述流程，保存编译、测试和验包记录，并将 IPA 保存到 packages。未包含签名证书、密码或描述文件。

## 来源和修改

上游源码基线：57a4e06a4caf162446d1c28caa7983f0493fecf0。

新增 assist/AssistRules、AssistSettings、ui/AssistButton、windows/WndAssist；在 Hero、Hunger、GameScene 接入；更改应用名称和 bundle ID；保留原 Java 类包名供 RoboVM 正确启动。英文／简体中文菜单，其他语言回退英文。保留原游戏资源与署名，详情见 README_UPSTREAM.md 和 LICENSE.txt。
