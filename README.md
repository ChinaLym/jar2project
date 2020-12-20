# 反编译工具

将 jar 反编译并转换为 maven 工程

## 步骤

- 找出需要反编译的 jar
    - 如：`lib` 目录下有几百个 jar，希望反编译的特定的 jar，如包路径为 `org.shoulder.framework` 的

- 分类处理
    - 是 war 或带有 WEB-INF ？
        - 这时，说明该 jar 是工程的启动模块，反编译反编译使用 WEB-INF/classes 中的，并输出提醒！
    - spring boot 的 starter.jar？
        - 这时，说明该jar包含了目标工程以及第三方的jar，比较特殊，如根目录的 org.spring 的启动类并不是希望反编译的，所有依赖的 class 都在 classes 目录中，且程序无法识别该反编译哪种jar，抛异常处理
            - 由使用者手动解压该 jar 并重新尝试整个流程即可。
    - 普通 jar
        - 直接反编译 jar

- 完善工程
    - 将 pom.xml 、资源文件 拷贝进对应位置
    - 将反编译出来的 unicode 进行转化

----

- 找出需要反编译的 jar
    - jar 中包含 `特定包路径` 或 `org.springframework.boot.loader`
    
        
- 判断是 war ? 
    - 存在 WEB-INF
            
- 判断是 spring boot 的 starter.jar ?
    - 存在 application.xml / yml / yaml
            
- 判断是 spring boot 的 starter.jar ?
    - 存在 application.xml / yml / yaml

