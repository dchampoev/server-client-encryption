package auth;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserServiceXml implements UserService {

    private final File file;
    private final Map<String, User> users = new ConcurrentHashMap<>();

    public UserServiceXml(String xmlPath) {
        this.file = new File(xmlPath);
        load(); // initial load
        if (users.isEmpty()) {
            users.put("admin", new User("admin", "admin",
                    EnumSet.of(Right.ADMIN, Right.ENCRYPT, Right.DECRYPT)));
        }
    }

    // ---------- PUBLIC API ----------
    public synchronized void load() {
        users.clear();
        if (!file.exists()) return;

        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(file);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("user");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node n = nodeList.item(i);
                if (n.getNodeType() != Node.ELEMENT_NODE) continue;

                Element e = (Element) n;
                String username = text(e, "username");
                String password = text(e, "password");
                String rightsStr = text(e, "rights");

                if (username == null || username.isBlank() || password == null) continue;

                EnumSet<Right> rights = parseRights(rightsStr);
                users.put(username, new User(username, password, rights));
            }
        } catch (Exception ex) {
            users.clear();
        }
    }

    public synchronized void save() {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = doc.createElement("users");
            doc.appendChild(root);

            List<String> usernames = new ArrayList<>(users.keySet());
            Collections.sort(usernames);

            for (String username : usernames) {
                User u = users.get(username);

                Element userEl = doc.createElement("user");

                Element un = doc.createElement("username");
                un.setTextContent(u.getUsername());
                userEl.appendChild(un);

                Element pw = doc.createElement("password");
                pw.setTextContent(u.getPassword());
                userEl.appendChild(pw);

                Element rs = doc.createElement("rights");
                rs.setTextContent(rightsToString(u.getRights()));
                userEl.appendChild(rs);

                root.appendChild(userEl);
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            File parent = file.getParentFile();
            if (parent != null) parent.mkdirs();

            transformer.transform(new DOMSource(doc), new StreamResult(file));
        } catch (Exception ex) {
            throw new RuntimeException("Failed to save users.xml", ex);
        }
    }

    public synchronized List<User> listUsers() {
        return users.values().stream()
                .sorted(Comparator.comparing(User::getUsername))
                .toList();
    }

    public synchronized void addOrUpdateUser(User user) {
        users.put(user.getUsername(), user);
    }

    public synchronized boolean deleteUser(String username) {
        return users.remove(username) != null;
    }

    @Override
    public User authenticate(String username, String password) {
        if (username == null || password == null) return null;
        User u = users.get(username);
        if (u == null) return null;
        return u.getPassword().equals(password) ? u : null;
    }

    // ---------- HELPERS ----------
    private static String text(Element element, String tag) {
        NodeList nl = element.getElementsByTagName(tag);
        if (nl.getLength() == 0) return null;
        return nl.item(0).getTextContent();
    }

    private static EnumSet<Right> parseRights(String rightsStr) {
        EnumSet<Right> rights = EnumSet.noneOf(Right.class);
        if (rightsStr == null || rightsStr.isBlank()) return rights;

        for (String r : rightsStr.split(",")) {
            String rr = r.trim();
            if (rr.isEmpty()) continue;
            try {
                rights.add(Right.valueOf(rr));
            } catch (IllegalArgumentException ignored) {}
        }
        return rights;
    }

    private static String rightsToString(EnumSet<Right> rights) {
        if (rights == null || rights.isEmpty()) return "";
        List<Right> rs = new ArrayList<>(rights);
        rs.sort(Comparator.comparing(Enum::name));
        StringBuilder sb = new StringBuilder();
        for (Right r : rs) sb.append(r.name()).append(",");
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }
}
