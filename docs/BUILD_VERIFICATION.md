# 最新925版验证

925已重新编译并发布，修复924的iOS运行库兼容性缺陷。详情见[闪退问题与修复](IOS_CRASH_924.md)、[新版IPA校验](verification/925-ipa-integrity.json)、[新版回归检查](verification/925-regression.json)。

- 源码提交：e8ea04c93c5e4b0e5428b4711fc1354593c7711f。
- [原生构建成功](https://github.com/sy-129/shattered-assist-ios/actions/runs/39351948716)，25项测试、345项运行库API引用检查通过。
- [Releases下载](https://github.com/sy-129/shattered-assist-ios/releases/tag/v4.0.2-assist.4)，GitHub服务器给出的IPA摘要与本地验包一致。
- [Releases及Packages发布流程](https://github.com/sy-129/shattered-assist-ios/actions/runs/39352614716)。
- IPA SHA256：02e126cb0c49b34ba4e8d5338318ec8821175cbf926903d2cc0f1cf01e9e8860，66,178,695字节。
- 源码ZIP SHA256：fabd6ed1791bee53682352f5f3696efbe6b7a51da4ef3e117eb0a828a77f6c17，68,909,152字节。
- REA命令：`rea inspect-artifact ShatteredAssist-925.ipa --format json`，证据`ev_a9ce61e14965518e3cbb7050df6610850af0c363d3524a08c2eebc19e03f62c5`。
- 925目标设备实装复验待用户反馈。以下为旧924交付时的历史记录；旧版随后发现运行库兼容性问题，请安装925。

---

# 4.0.2-assist.3 / 924 交付验证

- 源码提交：`8fea1c60a29849c7a02c5fdf9676ff1bc9c03de9`。
- [macOS 原生构建及测试成功](https://github.com/sy-129/shattered-assist-ios/actions/runs/39313708767)。
- [IPA 与源码下载](../packages/README.md)，IPA 66,177,374 字节。
- IPA SHA256：`e5bc28df26697b821c6aa6ddba1fe84d4f3998b2d4c0476adb1843e54239ff1d`。
- 源码 ZIP SHA256：`086b9078755e2c006ce2caa7577b966016640bc0e2ceac90d8464833c290670b`。

25 项自动测试通过，实际桌面场景输入验证通过，包括圆形方向盘、同方向事件去重、按钮拖动隐藏、家园往返保留角色生命及位置、真实骑乘速度乘1.25。桌面截图不是 iPhone 截图。

Apple 工具链完成 ARM64 AOT 编译与 codesign 深度校验。最终下载文件再次完成 ZIP CRC、主程序11676个代码页、3个特殊签名槽、518项资源哈希校验；11项外部素材原始哈希及许可证一致。源码 ZIP CRC、SHA256及主要源码与许可证一致。

用户提供的图已生成应用图标，并用于启动画面与仓库封面。生成的180×180图标已查看确认。

REA 命令：`rea inspect-artifact ShatteredAssist-8fea1c6.ipa --format json`，产物图检查完成，证据标识：`ev_b934d6f962733b5bd156567c55caec51ca47bf8e5e5b367a7c6faa3dd0b767e2`。REA 文件盘点本身不能证明真机运行；架构与签名由独立检查确认。

原始校验摘要：[IPA](verification/ipa-integrity.json)、[源码](verification/source-integrity.json)、[桌面交互](verification/desktop-checks.json)。

iPhone 16 Pro / 用户报告的 iOS 27.0.1 尚未实装验证。IPA 使用临时 ad-hoc 签名，需在轻松签中用自己的有效证书和描述文件重签。没有绕过系统签名限制。进入首页点“独立家园”，地牢内点“辅助”；隐藏辅助按钮后可从游戏菜单恢复。
