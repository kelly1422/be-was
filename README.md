## 프로젝트 정보 
이 프로젝트는 우아한 테크코스 박재성님의 허가를 받아 https://github.com/woowacourse/jwp-was 
를 참고하여 작성되었습니다.


### 목차
- [step1 - index.html 응답](#step1---indexhtml---)
- [step2 - GET으로 회원가입](#step2---get-------)
- [step3 - 다양한 컨텐츠 타입 지원](#step3----------------)
- [step4 - POST로 회원 가입](#step4---post-------)
- [step5 - 쿠키를 이용한 로그인](#step5--------------)
- [step6 - 동적인 HTML](#step6-------html)

<br/>

# step1 - index.html 응답
## 구현 내용
### 1. 정적인 html 파일 응답
경로에 입력한 파일을 찾아 리턴하도록 구현

### 2. HTTP Request 내용 출력
RequestDto 클래스를 만들고, 안에 해쉬맵 형태의 헤더 필드를 두어 헤더를 키-벨류 형식으로 저장하여, 필요한 헤더를 key 값을 통해 get 하여 사용할 수 있도록 하였다. 그 중 http 메소드와 경로와 host를 logger를 이용해 로그에 출력하도록 하였다.

### 3. Java Thread 기반으로 작성되어 있는 코드를 Concurrent 패키지를 사용하도록 구조 변경
`Executor executor = Executors.newFixedThreadPool(10);`
`executor.execute(new RequestHandler(connection));`
10개의 스레드를 가질 수 있는 스레드풀을 생성하고 Runnable한 RequestHandler 객체를 넘겨 실행하도록 하였다.

<br/>

## 고민 사항

http 요청 헤더 중 어떤 요소가 중요할지 고민했다. RequestDto 클래스를 생성하고 읽어들인 헤더의 키 값과 벨류를 저장할 map 필드를 생성해 헤더 값들을 저장하고, 필요한 헤더를 get하여 쓸 수 있게 하였다. 
하지만 해당 클래스를 처음 봤을 때, 어떤 헤더 값들이 저장되어 있는지 알 수 없다는 단점이 있다. 
이를 헤더가 각각의 필드를 가지게 변경해야 할지, 이러면 분기문으로 처리해야 하는데 이건 너무 불필요한 코드 인것 같아 고민이다.

---

Concurrent 패키지를 사용해 구조를 변경하는 과정에서 
`Executor executor = Executors.newFixedThreadPool(10);` 
위의 코드를 작성할 때, 몇 개의 스레드를 가지도록 스레드풀을 생성하면 좋을지 고민하였다. 
고민하며 스레드의 적정 개수에 대해 알아보았고, 
> 적정 스레드 개수 = CPU 코어 수 * (1 + 대기시간/작업시간)

보통 위의 공식으로 적정 스레드 수를 구하는 것을 알 수 있었다.
하지만 현재 제작하는 웹서버는 테스트할 때만 사용하기에 사용자가 여럿이 될 가능성이 거의 없어 그냥 10개로 지정해 스레드 풀을 생성하였다.

---

Runnable 객체들을 실행하기 위해 
`executor.execute() vs executor.submit() ` 중 무엇을 사용할지 고민했다.
execute은 그냥 실행만, submit은 실행 결과나 스레드에 대한 정보를 리턴받을 수 있다고 하는데, 결과는 필요 없어서 그냥 execute으로 실행하였다.


<br/>
<br/>
<br/>


# step2 - GET으로 회원가입
## 구현 내용
### 회원가입 기능
* 들어오는 요청을 파싱하고, 그에 맞는 컨트롤러-메소드를 매핑시켜 주었다.
* 커스텀 예외 클래스 WebServerException을 만들어, 비지니스 로직 처리 과정 중 발생하는 예외를 공통적으로 처리할 수 있도록 하였다.
* HttpStatus, ErrorCode, FilePath 의 enum을 만들어 상수 값들을 관리하도록 하였다.
* ResponseHandler를 작성해 body가 있을 때, 리다이렉트를 해야 할 때, 에러를 반환해야 할 때 실행하는 메소드를 작성하여 응답하도록 하였다.
### Junit을 활용한 단위 테스트 작성
* 가입이 잘 되었는지 테스트
*  중복 id, email 예외 처리가 잘 되는지 확인하였다.

<br/>

## 고민 사항
### Reflection 적용
파라미터 값을 파싱하고 User 객체를 생성하는 과정에서, 파라미터의 키 값을 하드코딩으로 넘겨줘야 했다. 하지만 파라미터들의 키 값과 User 필드의 각 필드명이 같았기에 Reflection을 적용하면 하드코딩으로 넘겨주지 않아도 돼 처음에 리플렉션을 적용해 작성했다.

* 리플렉션을 적용해 작성한 코드
```Java
public User(String params) {
        try {
            for (String param : params.split("&")) {
                String[] keyAndValue = param.split("=");
                String key = keyAndValue[0];
                String value = keyAndValue[1];

                Field field = this.getClass().getDeclaredField(key);
                field.setAccessible(true);
                field.set(this, value);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
}
```
하지만 작성해보니 코드가 생각보다 깔끔하지 않았고, 가독성이 떨어졌다.
또한 넘겨주는 파라미터의 키 값이 변경되면 User 의 필드명들을 그에 맞게 모두 바꿔줘야 했다.
결론적으로 Reflection을 쓰는 것보다 하드코딩으로 파라미터의 키 값들을 넘겨주는 것이 낫다고 생각해 코드를 변경했다.

* 파라미터의 키 값을 직접 입력하는 코드
```java
public User(Map<String, String> params) {
        this.userId = params.get("userId");
        this.password = params.get("password");
        this.name = params.get("name");
        this.email = params.get("email");
}
```

### 요청 메소드, 경로에 맞는 메소드 매핑
처음에는 if문으로 분기하여 그에 맞는 함수를 실행하도록 하려고 했다.
하지만 코드가 복잡해지는 것 같다 느꼈다. 이 문제를 다른 분들의 코드를 참조하여, 
메소드는와 달리 일급객체라 매개변수 등으로 쓸 수 있는 함수형 인터페이스인 BiConsumer를 사용해 해결했다.  
람다, 함수형 인터페이스 등에 대해 공부해 볼 수 있었다.

### 예외 처리를 어떻게 해야 할지
처음엔 컨트롤러에서 try-catch 문을 통해 서비스 코드 내에서 throw한 예외를 catch해 에러 응답을 보내주려 했다.
하지만 따로 처리해주지 않아도 throw 한 예외를 모두 클라이언트에 반환해주는 서블릿 컨테이너 방식을 흉내내 
컨트롤러가 아닌, 컨트롤러 메소드를 호출하는 상위 클래스에서 예외 처리를 공통적으로 해주도록 하였다.

### 서비스 내에서 유저 객체를 생성
서비스 클래스의 메소드 내에서 유저 객체를 생성하고 디비에 저장하는 모든 코드가 있어야 된다고 생각해 매개변수로는 파라미터를 받도록 했는데, 
이렇게 작성하니 테스트 코드에서 유저 객체가 아닌 파라미터 해쉬맵 객체들을 생성하게 되어.. 별로인 것 같다. 나중에 더 생각해봐야 겠다.


<br/>
<br/>
<br/>


# step3 - 다양한 컨텐츠 타입 지원
## 구현 내용

 * 1.  probeContentType 을 이용하여 다양한 컨텐트 타입을 지원
 * 2. GetMapping 어노테이션 작성 (리팩토링)
   
<br/>

## 고민 사항

### 1. Files.probeContentType
처음에는 파일의 확장자만 파싱하여 text/[확장자] 형식으로 ContentType을 응답 헤더에 넣어 전송하려 했지만, 
MIME tpye을 공부해보니 html, css, js 파일 외에 이미지의 경우는 image/png 등을 전송해줘야 했기에
내장된 probeContentType 메소드를 이용하여 파일의 컨텐트 타입을 쉽게 알아내고자 했다.
하지만 처음에 JDK 문제였던 것 같은데, 파일에 .(점)이 두개 이상 포함되어 있는 경우 파일의 컨텐트 타입을 제대로 알아내지 못하는 경우가 있었고 디버깅을 통해 해당 문제의 원인에 대해 알아냈다.

> 문제의 원인 : Map<확장자, MIME 타입> 의 맵이 있는데, 애초에 여기에 .css 확장자, .js 확장자에 대한 MIME 타입이 존재하지 않았던 것.


### 2. 확장성 좋은 구조
이전에는 개발자가 새로운 컨트롤러, 메소드를 작성하면, MethodMapper에서 새로 작성한 메소드를 직접 등록해주는 코드를 작성해야 했다.
확장성이 좋지 않은 번거로운 코드라고 생각을 했고, 자동으로 MethodMapper에 등록할 수 있는 방법이 있을지 고민했다.
이전 팀원분들이 리플렉션, 어노테이션을 활용하신 점을 보고 스프링처럼 **`GetMapping`** 어노테이션을 작성하고  **`리플렉션`** 을 이용하여 자동으로 등록되도록 구현해보았다. 이후 확장성을 고려하였을 때 PostMapping, DeleteMapping 등 어노테이션을 추가하고 간단한 코드만 추가하면 돼서 **`가독성, 확장성`** 이 더 좋아진 것 같다.

## 기타

### redirection 상태 코드

> 302(임시 이동): 현재 서버가 다른 위치의 페이지로 요청에 응답하고 있지만 요청자는 향후 요청 시 원래 위치를 계속 사용해야 한다. 
> 303(기타 위치 보기): 요청자가 다른 위치에 별도의 GET 요청을 하여 응답을 검색할 경우 서버는 이 코드를 표시

302로 했다가 303으로 바꿨는데, 회원가입후 메인 페이지로 리다이렉트 하도록 하였는데 이 경우에 어떤 코드가 더 맞는건지 아직 고민이다.
다음 과제에서 302로 하라고 해서 302로 할 예정인데, 더 알아봐야 겠다.

<br/>
<br/>
<br/>

# step4 - POST로 회원 가입
## 구현 내용
### java.nio 에서 java.io 로 변환
* Files.readAllBytes 대신 io.FileInputStream 의 read를 이용하여 파일을 바이트 배열로 변환
* Files.probeContentType 대신 FileContentType enum을 생성해 적용
### 회원가입 POST로 수정
* PostMapping 어노테이션 작성
* MethodMapper 내에 PostMapping 어노테이션을 가진 메소드를 등록하는 코드 작성
* Request의 body 읽어들이기 -요청 헤더의 Content-Length 값을 불러와, BufferedReader.read 를 통해 읽어 파싱해 Map 으로 저장

<br/>

## 고민 사항
### enum - 효과적으로 Content-Type을 반환하는 방법
확장자를 파싱한 후, 분기문을 통해 확장자별로  Content-Type을 반환하도록 하면, enum을 사용한 이유가 별로 없는 것 같다는 생각을 했다.
확장자를 통해 FileContentType enum객체를 불러올 수 있는 방법을 고민했고
```java
private static final Map<String, FileContentType> fileContentTypeMap = Collections.unmodifiableMap(
    Stream.of(values()).collect(Collectors.toMap(FileContentType::getExtension, Function.identity()))
);

public static FileContentType of(String extension) {
    return fileContentTypeMap.get(extension);
}
```
enum 내부에 위의 코드를 통해 `Map<확장자, FileContentType>` 의 static 변수를 생성해두고,
`FileContentType.of` 메소드를 통해 확장자로부터 그에 맞는 FileContentType enum객체를 가져올 수 있도록 하였다.

```java
public static String getContentType(String path) throws IOException {
    int extensionPoint = path.lastIndexOf(".");
    return FileContentType.of(path.substring(extensionPoint)).getContentType();
}
```
최종적으로 위와 같은 코드를 통해 파일의 Content-Type을 반환하도록 구현하였다.

## 기타
* 어노테이션도 상속받을 수 있으면 좋을 것 같다는 생각을 했다.

<br/>
<br/>
<br/>

# step5 - 쿠키를 이용한 로그인
## 구현 내용
### 세션
* 랜덤한 세션 아이디를 생성하는 메소드
* 세션을 보관할 시간을 설정, 세션 만료 기능
### 로그인 기능
* POST 로 /user/login 경로로 들어온 요청 처리 (로그인한 유저가 또 로그인한 경우 고려)
* 회원가입한 유저인지, 틀린 비밀번호를 입력했는지 확인하고 예외 처리
* 로그인에 성공하면 응답 헤더의Set-Cookie에 생성한  senssionId를 보내주기
### 응답 기능
* ResponseDto 클래스 생성
* ResponseHandler의 send200, sendError 등의 메소드를 send()로 통일

<br/>

## 고민 사항
### 1. ResponseHandler
기존의 방식은 http상태 코드별로 응답을 전송하는 메소드를 만들어 사용했었다.
body가 있는 경우 `sendBody()`, 
리다이렉트 해야하는 경우는 `sendRedirect()`, 
에러가 발생한 경우 `sendError()`를 호출해 응답을 보냈는데,
나머지는 다 똑같고 헤더만 하나 추가되도, 해당 응답을 전송하기 위해 ResponseHandler에 새로운 메소드를 작성해야 했다.
이런 식으로 응답의 종류별로 메소드를 하나씩 생성하는 것은 확장성이 좋지 않고 유지보수가 굉장히 힘든 코드임을 느꼈고,
이를 개선하기 위해 ResponseDto를 생성해 좀 더 유연하게 응답을 생성하고,
ResponseHandler에는 ResponseDto를 매개변수로 받아 응답을 전송하는 send() 메소드 하나만 두어,
생성한 응답을 공통적으로 전송할 수 있게 하였다.
```java
public static void send(DataOutputStream dos, ResponseDto responseDto) {
        try {
            // 헤더 전송
            dos.writeBytes("HTTP/1.1 " + responseDto.getStatus().toString() + " \r\n");
            for (String header : responseDto.getHeaders()) {
                dos.writeBytes(header);
        }

        dos.writeBytes("\r\n");

        // 바디가 있는 경우 바디 전송
        if (responseDto.getBody() != null) {
            dos.write(responseDto.getBody(), 0, responseDto.getBody().length);
            dos.flush();
        }

    } catch (IOException e) {
        logger.error(e.getMessage());
    }
}
```
### 2. 세션 만료
`Timer 사용` vs `Session.getAttribute 메소드 내부에서 접근하기 전에 한 번`
세션을 만료하는 코드를 고민해봤을 때, 먼저 Timer를 이용한 방법을 생각했다.
Timer의 스레드를 이용하여, 주기적으로 세션 만료를 검사하는 메소드 `checkSessionExpiration()` 를 실행하는 것이다.

하지만 매초 저장되어 있는 모든 세션 값들의 마지막으로 읽혀진 시간을 통해,
세션 유효시간이 지났는지 확인하고, 지우는 작업을 매초 실행하는 것이 비효율적이라는 생각을 했다.
그냥 특정 세션 값에 접근하기 전에 해당 세션의 만료 여부를 체크해주는 것이 훨씬 효율적이라 생각해 이런 방식으로 구현하였다.

```java
// 세션에 접근
public static String getAttribute(String sessionId) {
    // 접근하기 전에 해당 세션의 만료 여부를 체크
    checkSessionExpiration(sessionId);
    String userId;
    if ((userId = session.get(sessionId)) != null) {        
        lastAccessTime.put(sessionId, System.currentTimeMillis());
    }
    return userId;
}

// 특정 세션 값의 만료 여부를 확인하고 제거하는 메소드
private static void checkSessionExpiration(String sessionId) {
    if (lastAccessTime.containsKey(sessionId) &&
        lastAccessTime.get(sessionId) + INVALIDATE_TIME < System.currentTimeMillis())
        removeSession(sessionId);
}
```
### 3. enum 필드 private → public
enum의 필드를 private으로 두고, get 메소드를 통해 필드의 값을 가져와 사용했었는데,
어차피 final 필드이기에 변경할 수 없는 값이라 private으로 선언할 필요가 없다고 생각했다.
public으로 바꾸고 get 메소드는 제거하여 그냥 필드에 직접 접근할 수 있도록 변경하였다.


<br/>
<br/>
<br/>

# step6 - 동적인 HTML
## 구현 내용
### 리팩토링
* 사용자의 요청을 그에 맞는 핸들러와 매핑 시켜주는 Dispather
* 파일 컨텐츠 요청을 처리하는 FileContentHandler
* api 요청을 처리하는 ApiHandler
* 요청한 사용자의 세션을 ThreadLocal에 저장하여 스레드 전역에서 관리한다.
### 동적인 html 구현
* 사용자가 로그인 상태일 경우 /index.html에서 사용자 이름을 표시해 준다.
* 사용자가 로그인 상태일 경우 네비게아션바에 `로그인`, `회원가입` 버튼을 보여주지 않는다.
* 사용자가 로그인 상태가 아닐 경우 /index.html에서 `로그인` 버튼을 표시해 준다.
* 사용자가 로그인 상태가 아닐 경우 네비게아션바에 `로그아웃`, `개인정보수정` 버튼을 보여주지 않는다.
* 사용자가 로그인 상태가 아닌데 회원만 사용할 수 있는 요청을 
* 사용자가 로그인 상태일 경우 http://localhost:8080/user/list 에서 사용자 목록을 출력한다.
* http://localhost:8080/user/list  페이지 접근시 로그인하지 않은 상태일 경우 로그인 페이지(login.html)로 이동한다.
### 로그아웃 기능
* 로그인한 사용자가 http://localhost:8080/user/logout 으로 Get 요청을 보낸 경우 앱 내 해당 사용자의 세션을 지우고 로그아웃 처리한다.
* /index.html 로 리다이렉트
  
<br/>

## 고민 사항
### 전체적인 흐름을 잘 파악할 수 있도록 리팩토링
처음엔 요청을 읽어들인 후 바로 실행되는 디스패처 run() 부분에서 전체적인 코드 흐름을 잘 알 수 없었다.
이 부분을 개선하고, 가독성을 좋게 하고자 ApiHandler, FileContentHandler 클래스를 분리하여 로직을 작성하였다.
```java
// API 호출 요청인지 확인
httpResponse = ApiHandler.handle(httpRequest);
// 파일 컨텐츠 요청인지 확인 후 처리
if (httpResponse == null) {
    httpResponse = FileContentHandler.handle(httpRequest);
}
```
### html 파일을 동적으로 처리하는 방법
처음엔 html 파일을 String으로 읽어와 마크해 둔 부분을 기점으로 스플릿하고.. 붙이고 할 생각이었는데
replace를 잊고 있었다. `replace`를 사용해 쉽게 구현했다.
### 로그인하지 않은 사용자가 특정 요청을 호출하면 로그인 페이지로 리다이렉트
디스패처에서 처음에 사용자의 요청을 파싱한 직후에 요청 헤더의 쿠키 헤더의 sessionId 값으로 ThreadLocal을 생성하여 스레드 전역에서 세션을 관리할 수 있도록 하였다.
이후 FileConstant 에 static final List<String> 으로 API_CAN_EXECUTE_WITHOUT_LOGIN, FILE_CAN_READ_WITHOUT_LOGIN 을 초기화 해두고 아래와 같이 처리해주도록 하였다.
```java
// 로그인하지 않은 사용자가 로그인한 사용자만 사용할 수 있는 기능에 접근하면 로그인 페이지로 리다이렉트
if (ThreadLocalManager.getSession() == null && 
    !API_CAN_EXECUTE_WITHOUT_LOGIN.contains(httpRequest.getPath())) {
        ResponseHandler.makeRedirect(httpResponse, LOGIN_PAGE_PATH);
}
```
### 세션을 사용하는 법
세션은 스레드 전역에서 접근할 경우가 많다. 로그인 여부를 확인할 때, 로그인한 사용자의 닉네임을 가져오는 경우 등
하지만 이런 순간마다
> 1. 요청헤더의 쿠키에서 sessionId 값을 가져오고
> 2. SessionManager 를 통해 해당 세션 아이디의 세션을 불러오고
> 3. 해당 세션의 getAttribute나 setAttribute를 진행한다.

이런 복잡한 과정이 반복되는 것은 별로인 것 같다는 생각을 했다.
그래서 SessionId와 Session을 ThreadLocal로 관리하면 좋을 것 같다고 생각해 ThreadLocalManager를 작성했다.
이후에는 처음에 요청을 파싱할 따에만 쿠키 값에 접근해 sessionId를 가져와 ThreadLocalManager를 초기화하고, 
이후에 세션 정보가 필요할 때,
> ThreadLocalManager.getSession()으로 바로 세션 값을 가져와서 사용할 수 있다.

덕분에 전체적으로 코드가 깔끔해진 것 같다.

<br/>

## 기타
테스트 코드의 중요성을 알게 되었다.
getMimeType 메소드 호출시 매개변수인 path 가 null일 때, 확장자가 없는 경우 고려한 예외처리를 원래 하지 않았는데
테스트 코드를 작성하면서 이런 부분의 예외처리를 추가할 수 있게 되었다.
