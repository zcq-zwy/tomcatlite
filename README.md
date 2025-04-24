## 简化版J2EE服务器（HTTP服务器和Servlet容器）

## 具备的功能(均为简化版的实现)：
1.HTTP Protocol 2.Servlet  3.ServletContext 3.Request和Response 4.DispatcherServlet 5.可以进行Static Resources访问 和 File Download
6.Error Notification错误提醒  7.Get & Post & Put & Delete支持4种访问方法  8.可以进行web.xml parse和Forward，Redirect 
9.一个简单的模版引擎Simple TemplateEngine 10.session和cookie 11.servlet容器的servlet filter listener

## http服务器设计
主从多reactor设计
![image](https://github.com/user-attachments/assets/140585b3-b415-4f13-ad22-bf7f3069a529)
1.acceptor采用selector监听accept事件，之后select同步返回，进行accept，返回客户端socketchannel，之后放入到 ConcurrentLinkedQueue
2.poller消费ConcurrentLinkedQueue里的socketchannel，之后包装成pollevent进行消费，也就是注册selector并关注读事件
3.当读事件来了以后，进行read事件的回调
4.回调成功，进行业务处理，将业务处理包装成httpprocessor放入线程池进行处理

## servlet容器设计

![image](https://github.com/user-attachments/assets/7c1c8380-475a-4fe2-b92f-60d809c9c283)


![image](https://github.com/user-attachments/assets/d52242b2-0263-483d-a3af-10c32a080fe4)
