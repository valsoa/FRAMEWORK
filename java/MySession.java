package util;

import jakarta.servlet.http.HttpSession;
public class MySession {
    private HttpSession requestSession;

   public Object get(String key){
    return requestSession.getAttribute(key);
   }

   public void add(String key, Object object){
    requestSession.setAttribute(key,object);
   }
   public void delete(String key){
    requestSession.removeAttribute(key);
   }

public MySession(HttpSession requestSession) {
    this.requestSession = requestSession;
}
   
}
