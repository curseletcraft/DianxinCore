[version]: https://api.bintray.com/packages/jagrosh/maven/JDA-Utilities/images/download.svg
[download]: https://bintray.com/jagrosh/maven/JDA-Utilities/_latestVersion
[license]: https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg
[issues]: https://img.shields.io/github/issues/JDA-Applications/JDA-Utilities.svg
[issues-link]: https://github.com/JDA-Applications/JDA-Utilities/issues

[//]: # ([ ![version][] ][download])
[//]: # ([ ![license][] ]&#40;https://github.com/JDA-Applications/JDA-Utilities/tree/master/LICENSE&#41;)
[//]: # ([ ![issues][] ][issues-link])

# 🤖 DianxinCore

Một framework đơn giản và tiện lợi dùng để làm bot Discord bằng Java (sử dụng JDA làm thư viện chính).

Thể loại: Open-source framework

------

## Getting Started
Bạn cần phải thêm dependency (hoặc tải file jar từ repository trên) giống như
[JDA](https://github.com/DV8FromTheWorld/JDA).

Với maven:
```xml
  <dependency>
    <groupId>com.github.CurseletCraft</groupId>
    <artifactId>DianxinCore</artifactId>
    <version>2.0.0</version>
  </dependency>
        
  <!-- https://mvnrepository.com/artifact/net.dv8tion/JDA -->
  <dependency>
    <groupId>net.dv8tion</groupId>
    <artifactId>JDA</artifactId>
    <version>6.3.1</version>
  </dependency>
```
```xml
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
```

Với gradle:
```groovy
dependencies {
    implementation 'com.github.CurseletCraft:DianxinCore:2.0.0'
    
    // optional
    // bỏ đi nếu bạn muốn sử dụng phiên bản JDA có sẵn trong DianxinCore
    implementation("net.dv8tion:JDA:6.3.1")  
}

repositories {
    maven { url 'https://jitpack.io' }
    mavenCentral() // import maven central cho JDA
}
```

[//]: # (## Examples)

[//]: # (Check out the [ExampleBot]&#40;https://github.com/jagrosh/ExampleBot&#41; for a simple bot example.)

[//]: # ()
[//]: # (Other guides and information can be found on the [wiki]&#40;https://github.com/JDA-Applications/JDA-Utilities/wiki&#41;.)

[//]: # (## Projects)

[//]: # ([**Vortex**]&#40;https://github.com/jagrosh/Vortex&#41; - Vortex is an easy-to-use moderation bot that utilizes the JDA-Utilities library for the Command Client and some of the menus<br>)

[//]: # ([**JMusicBot**]&#40;https://github.com/jagrosh/MusicBot&#41; - This music bot uses the Command Client for its base, and several menus, including the OrderedMenu for search results and the Paginator for the current queue<br>)
[//]: # ([**GiveawayBot**]&#40;https://github.com/jagrosh/GiveawayBot&#41; - GiveawayBot is a basic bot for hosting quick giveaways!<br>)