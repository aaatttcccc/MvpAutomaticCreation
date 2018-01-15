package dialog;

import compute.AppNames;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 添加appName进缓存
 */
public class DialogAppName extends JDialog {

    //region view
    private JPanel contentPane;
    private JTextField txtAppName;
    private JButton btnCancel;
    private JButton btnOK;
    //endregion

    private SelectCallBack mSelectCallBack; // 回调函数

    public DialogAppName() {

    }

    /**
     * 构造函数
     *
     * @param selectCallBack 回调
     */
    public DialogAppName(SelectCallBack selectCallBack) {
        mSelectCallBack = selectCallBack;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(btnOK);

        setTitle("添加appName"); // 设置title
        setSize(600, 400); // 设置窗口大小

        // 设置窗口位置
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        int Swing1x = 600;
        int Swing1y = 300;
        setBounds((screensize.width - Swing1x) / 2, (screensize.height - Swing1y) / 2 - 100, Swing1x, Swing1y);

        btnOK.addActionListener(e -> {
            if (btnOK.isEnabled())
                onOK();
        });

        btnCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // 添加键盘输入事件
        txtAppName.getDocument().addDocumentListener(new MyDocumentListener());
    }

    public static void main(String[] args) {
        DialogAppName dialog = new DialogAppName();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private void onOK() {
        this.commit(); // 提交
        this.mSelectCallBack.select("");
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    /**
     * 提交
     */
    private void commit() {
        // 初始化
        AppNames.initData();

        String name = this.txtAppName.getText();
        if (name != null && name.trim().length() > 0) {
            // 添加
            Map<String, String> mapData = new HashMap<>();
            mapData.put(AppNames.APPNAME, name.toUpperCase().trim());
            AppNames.addData(mapData);
        }
    }

    /**
     * 文本改变事件
     */
    class MyDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            if (isCommit())
                btnOK.setEnabled(true);
            else
                btnOK.setEnabled(false);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            if (isCommit())
                btnOK.setEnabled(true);
            else
                btnOK.setEnabled(false);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {

        }
    }

    /**
     * 是否能提交
     *
     * @return Boolean
     */
    private boolean isCommit() {
        boolean isCommit = false;

        String name = this.txtAppName.getText();
        if (name != null && name.trim().length() > 0)
            isCommit = true;

        return isCommit;
    }

}
