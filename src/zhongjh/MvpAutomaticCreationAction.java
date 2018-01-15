package zhongjh;

import com.google.common.base.CaseFormat;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * 执行类
 * （1）会获取包名，然后读取模板文件，替换模板文件中动态字符，在Dialog输入的作者和模块名称也会替换模板中字符，
 * （2）最后通过包名路径生成类文件
 * <p>
 * 后面我会根据实际工作需求，会想办法改进选择生成fragment还是activity
 * 而作者名称也应该能设置是默认的
 */
public class MvpAutomaticCreationAction extends AnAction {

    private JTextArea jTextArea;

    private Project project;
    private String mAppName = "";// 项目名称 比如app_company或者App
    private String mPackageName = "";// 包名 例如 com.yaoguang.shipper.home.test
    private String mAuthor;//作者
    private String mModuleName;//模块名称
    private String mModuleNameChinese; // 中文模块名称


    /**
     * 创建类型枚举
     */
    private enum CodeType {
        FragmentListCondition, ContractListCondition, PresenterListCondition, Activity, Fragment, Contract, Presenter
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        if (e == null)
            return;
        // 找出统计文件 2017/3/18 19:59
        ToolWindow toolWindow = ToolWindowManager.getInstance(Objects.requireNonNull(e.getProject())).getToolWindow("Compute Code Lines");
        if (toolWindow != null) {

            // 无论当前状态为关闭/打开，进行强制打开ToolWindow 2017/3/21 16:21
            toolWindow.show(() -> {

            });

            // ToolWindow未初始化时，可能为空 2017/4/4 18:20
            try {
                jTextArea = (JTextArea) ((JScrollPane) Objects.requireNonNull(toolWindow.getContentManager().getContent(0))
                        .getComponent().getComponent(0)).getViewport().getComponent(0);
            } catch (Exception ignored) {

            }
        }

        // TODO: insert action logic here
        project = e.getData(PlatformDataKeys.PROJECT);


        mPackageName = getPackageName2(e);

        // 作者名字
        mAuthor = System.getProperty("user.name");

        init();
        refreshProject(e);
    }

    /**
     * 刷新项目
     *
     * @param anActionEvent 选项操作
     */
    private void refreshProject(AnActionEvent anActionEvent) {
        Objects.requireNonNull(anActionEvent.getProject()).getBaseDir().refresh(false, true);
    }

    /**
     * 初始化Dialog
     */
    private void init() {
        MvpAutomaticCreation myDialog = new MvpAutomaticCreation((moduleName, moduleNameChinese, type, appName) -> {
            // 实例化ok事件,appName暂时作废
            mModuleName = moduleName;
            mModuleNameChinese = moduleNameChinese;

            createClassFiles(type);
            Messages.showInfoMessage(project, "生成MVP代码成功", "title");
        });
        myDialog.setVisible(true);

    }

    /**
     * 生成类文件
     */
    private void createClassFiles(int type) {
        switch (type) {
            case 0:
                // 生成Activity
                createClassFile(CodeType.Activity);
                break;
            case 1:
                // 生成Fragment
                createClassFile(CodeType.Fragment);
                createClassFile(CodeType.Contract);
                createClassFile(CodeType.Presenter);
                break;
            case 2:
                // 生成列表Fragment
                createClassFile(CodeType.FragmentListCondition);
                createClassFile(CodeType.ContractListCondition);
                createClassFile(CodeType.PresenterListCondition);
                break;
        }

//        createBaseClassFile(CodeType.BaseView); // 暂时作废
//        createBaseClassFile(CodeType.BasePresenter); // 暂时作废
    }

    /**
     * 生成mvp框架代码
     *
     * @param codeType 类型
     */
    private void createClassFile(CodeType codeType) {
        String fileName;
        String content;
        String appPath = getAppPath();
        switch (codeType) {
            case Activity:
                fileName = "TemplateActivity.txt";
                content = ReadTemplateFile(fileName);
                content = dealTemplateContent(content);
                writeToFile(content, appPath + mModuleName.toLowerCase(), mModuleName + "Activity.java");
                break;
            case Fragment:
                fileName = "TemplateFragment.txt";
                content = ReadTemplateFile(fileName);
                content = dealTemplateContent(content);
                writeToFile(content, appPath + mModuleName.toLowerCase(), mModuleName + "Fragment.java");
                break;
            case FragmentListCondition:
                fileName = "ListCondition/TemplateFragment.txt";
                content = ReadTemplateFile(fileName);
                content = dealTemplateContent(content);
                writeToFile(content, appPath + mModuleName.toLowerCase(), mModuleName + "Fragment.java");
                break;
            case ContractListCondition:
                fileName = "ListCondition/TemplateContract.txt";
                content = ReadTemplateFile(fileName);
                content = dealTemplateContent(content);
                writeToFile(content, appPath + mModuleName.toLowerCase(), mModuleName + "Contract.java");
                break;
            case PresenterListCondition:
                fileName = "ListCondition/TemplatePresenter.txt";
                content = ReadTemplateFile(fileName);
                content = dealTemplateContent(content);
                writeToFile(content, appPath + mModuleName.toLowerCase(), mModuleName + "Presenter.java");
                break;
            case Contract:
                fileName = "TemplateContract.txt";
                content = ReadTemplateFile(fileName);
                content = dealTemplateContent(content);
                writeToFile(content, appPath + mModuleName.toLowerCase(), mModuleName + "Contract.java");
                break;
            case Presenter:
                fileName = "TemplatePresenter.txt";
                content = ReadTemplateFile(fileName);
                content = dealTemplateContent(content);
                writeToFile(content, appPath + mModuleName.toLowerCase(), mModuleName + "Presenter.java");
                break;
        }
    }

    /**
     * 生成
     *
     * @param content   类中的内容
     * @param classPath 类文件路径
     * @param className 类文件名称
     */
    private void writeToFile(String content, String classPath, String className) {
        try {
            File floder = new File(classPath);
            if (!floder.exists()) {
                floder.mkdirs();
            }

            File file = new File(classPath + "/" + className);
            if (!file.exists()) {
                file.createNewFile();
            }
            OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file.getAbsoluteFile()),"UTF-8");
//            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(write);
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * 替换模板中字符
     *
     * @param content 内容
     * @return 返回内容
     */
    private String dealTemplateContent(String content) {
        content = content.replace("$name", mModuleName);
        content = content.replace("$lowerCaseName",mModuleName.toLowerCase());
        // 比如 yourName 转换成 your_name,一般用于布局名称多点
        content = content.replace("&_name",CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, mModuleName));
        // 中文模块名称
        content = content.replace("$moduleNameChinese",mModuleNameChinese);

        if (content.contains("$packagename")) {
            content = content.replace("$packagename", mPackageName + "." + mModuleName.toLowerCase());
        }
        if (content.contains("$packageappname")) {
            content = content.replace("$packageappname", mPackageName);
        }
        if (content.contains("$basepackagename")) {
            content = content.replace("$basepackagename", mPackageName + ".base");
        }

        content = content.replace("$author", mAuthor);
        content = content.replace("$date", getDate());
        return content;
    }

    /**
     * 获取包名文件路径
     */
    private String getAppPath() {
        String packagePath = mPackageName.replace(".", "/");

        return project.getBasePath() + "/" + mAppName + "/src/main/java/" + packagePath + "/";
    }

    /**
     * 获取当前时间
     */
    private String getDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        return formatter.format(currentTime);
    }

    /**
     * 根据菜单选择的文件路径
     *
     * @return anActionEvent 选项操作
     */
    private String getPackageName2(AnActionEvent anActionEvent) {
        String package_name = "";
        // 获取当前选择的文件或文件夹路径 2017/3/24 15:48
        VirtualFile data = anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (data == null) {
            if (jTextArea != null)
                jTextArea.append("找不到文件.\n");

            return "";
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();

            String appFile = data.getPath().substring(Objects.requireNonNull(project.getBasePath()).length() + 1);// 截取project.getBasePath()后面的文字
            mAppName = appFile.substring(0, appFile.indexOf("/"));
            Document doc = db.parse(project.getBasePath() + "/" + mAppName + "/src/main/AndroidManifest.xml");

            NodeList nodeList = doc.getElementsByTagName("manifest");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                Element element = (Element) node;
                package_name = element.getAttribute("package");
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return package_name;
    }

    /**
     * 读取模板文件中的字符内容
     *
     * @param fileName 模板文件名
     */
    private String ReadTemplateFile(String fileName) {
        InputStream in;
        in = this.getClass().getResourceAsStream("/zhongjh/Template/" + fileName);
        String content = "";
        try {
            content = readStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * 读取数据
     *
     * @param inputStream 二进制流
     */
    private String readStream(InputStream inputStream) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            outputStream.close();
            inputStream.close();
        }
        return outputStream.toString("UTF-8");
    }

}
