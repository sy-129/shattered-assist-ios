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
