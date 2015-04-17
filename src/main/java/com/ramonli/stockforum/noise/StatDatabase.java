package com.ramonli.stockforum.noise;

import org.h2.tools.Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Date;

/**
 * Export CSV from h2 database. Execute below command in shell,
 * 
 * <pre>
 * sql> call CSVWRITE ( 'f:/tmp/MyCSV.txt', 'SELECT * FROM forum_stat' )
 * </pre>
 * 
 * @author Ramon
 */
public class StatDatabase {

    public static void main(String[] args) throws Exception {
        // start the TCP Server
        Server server = Server.createTcpServer(args).start();

        Class.forName("org.h2.Driver");
        Connection conn = DriverManager.getConnection("jdbc:h2:~/forumstat", "sa", "");
        conn.setAutoCommit(false);

        PreparedStatement ps = conn.prepareStatement("insert into forum_stat(share_code,issue_date,count_of_issue,"
                + "count_of_view,count_of_response) values(?,?,?,?,?)");
        ps.setString(1, "600405");
        ps.setDate(2, new java.sql.Date(new Date().getTime()));
        ps.setInt(3, 28);
        ps.setInt(4, 43);
        ps.setInt(5, 18920);
        ps.execute();
        ps.close();
        conn.commit();

        // add application code here
        conn.close();

        // stop the TCP Server
        server.stop();
    }
}
