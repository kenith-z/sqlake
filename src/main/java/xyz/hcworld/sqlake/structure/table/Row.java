package xyz.hcworld.sqlake.structure.table;

/**
 * @ClassName: Row
 * @Author: 张冠诚
 * @Date: 2022/7/14 11:26
 * @Version： 1.0
 */
public class Row implements Cloneable {

    Long id;
    String username;
    String email;

    @Override
    public String toString() {
        return "Row{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }


    @Override
    protected Object clone()  {
        Row row = new Row();
        row.id = id;
        row.username = username;
        row.email = email;
        return (Row)row;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
