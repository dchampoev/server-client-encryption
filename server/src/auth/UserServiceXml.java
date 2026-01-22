package auth;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserServiceXml implements UserService {
    private final Map<String, User> users = new ConcurrentHashMap<>();

    public UserServiceXml(String xmlPath) {
        File file = new File(xmlPath);
        if(!file.exists()) {
            //default admin just for testing
            users.put("admin",new User("admin","admin",
                    EnumSet.of(Right.ADMIN,Right.ENCRYPT,Right.DECRYPT)));
            return;
        }

        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("user");

            for(int i = 0; i < nodeList.getLength(); i++) {
                Node n =  nodeList.item(i);
                if(n.getNodeType() != Node.ELEMENT_NODE) continue;

                Element e = (Element) n;

                String username = text(e,"username");
                String password = text(e,"password");
                String rightsStr = text(e,"rights");

                if(username==null||username.isBlank()||password==null) continue;

                EnumSet<Right> rights = EnumSet.noneOf(Right.class);
                if(rightsStr != null && !rightsStr.isBlank()) {
                   for(String r: rightsStr.split(",")) {
                       String rr = r.trim();
                       if(!rr.isEmpty()) {
                           try {
                               rights.add(Right.valueOf(rr));
                           } catch (IllegalArgumentException ignored){}
                       }
                   }
                }
                users.put(username,new User(username,password,rights));
            }
        } catch (Exception ex) {
            //If XML is broken, a default admin is placed
            users.clear();
            users.put("admin",new User("admin","admin",
                    EnumSet.of(Right.ADMIN,Right.ENCRYPT,Right.DECRYPT)));
        }
    }

    private static String text(Element element, String tag) {
        NodeList nl = element.getElementsByTagName(tag);
        if(nl.getLength() == 0) return null;
        return nl.item(0).getTextContent();
    }

    @Override
    public User authenticate(String username, String password) {
        if (username == null || password == null) return null;
        User u = users.get(username);
        if (u == null) return null;
        return u.getPassword().equals(password) ? u : null;
    }
}
