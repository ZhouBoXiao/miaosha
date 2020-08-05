package miaosha.mybatis.vo;

import lombok.Getter;
import lombok.Setter;
import miaosha.mybatis.entity.User;

import java.io.Serializable;

@Setter
@Getter
public class TeacherListVo implements Serializable {


    private String tId ;

    private Integer uId ;

    private String tName ;

    private User userList;

    @Override
    public String toString() {
        return "TeacherListVo{" +
                "tId='" + tId + '\'' +
                ", uId=" + uId +
                ", tName='" + tName + '\'' +
                ", userList=" + userList +
                '}';
    }
}
