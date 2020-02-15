package com.github.llyb120.namilite.boost;

import com.github.llyb120.json.Obj;
import org.beetl.sql.core.SQLManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static com.github.llyb120.json.Json.o;

public abstract class DyFunc {

    @Autowired
    @Qualifier(value = "udsql")
    SQLManager sqlManager;

    public abstract Object run(Obj params) throws Exception;

    public List<Obj> query(String sql, Obj params){
        return sqlManager.execute(sql, Obj.class, params);
    }

    public List<Obj> query(String sql){
        return query(sql, o());
    }

    public Object test(Obj params){
        //查询指定用户
        List<Obj> users = query("select id,username from t_user where username = #username#");
        if (users.isEmpty()) {
            return null;
        }
        //查询所有的部门
        Obj user = users.get(0);
        user.aa("os").addAll(query("select id,o.name from t_user_dep ud inner join t_org o on o.id = ud.did where ud.uid = #id#", o("id", user.ss("id"))));
        //查询所有的角色
        user.aa("os").addAll(query("select o.name from t_user_org ud inner join t_org o on o.id = uo.oid where ud.uid = #id#", o("id", user.ss("id"))));
        return user;
    }


}
