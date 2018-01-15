package zhongjh;

import compute.AppNames;

import javax.swing.*;
import java.awt.event.*;

public class MvpAutomaticCreation extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField txtClassName;
    private JRadioButton rbActivity;
    private JRadioButton rbFragment;
    private JRadioButton rbFragmentList;
    private JTextField txtClassNameChinese;
    private JComboBox cbAppName;

    private DialogCallBack mCallBack;

    /**
     * 在自动创建该类的时候，添加一个回调函数DialogCallBack，并且改变了onOK这个方法
     *
     * @param callBack 回调函数
     */
    public MvpAutomaticCreation(DialogCallBack callBack) {
        this.mCallBack = callBack;
        AppNames.initData();

        setTitle("MvpAutomaticCreation");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setSize(400, 300);
        setLocationRelativeTo(null);

        // 点击事件
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        // 选择事件
        ButtonGroup bgroup = new ButtonGroup();
        bgroup.add(rbActivity);
        bgroup.add(rbFragment);
        bgroup.add(rbFragmentList);

        // 添加数据源
        for (String s : AppNames.mapData.keySet()) {
            cbAppName.addItem(AppNames.mapData.get(s).get(AppNames.APPNAME));
        }

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        if (null != mCallBack) {
            // 判断类型
            int type = -1;
            if (rbActivity.isSelected()) {
                type = 0;
            } else if (rbFragment.isSelected()) {
                type = 1;
            } else if (rbFragmentList.isSelected()) {
                type = 2;
            }

            mCallBack.ok(txtClassName.getText().trim(), txtClassNameChinese.getText().trim(), type,String.valueOf(cbAppName.getSelectedItem()));
        }
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    // 这个作废，去掉，无用
//    public static void main(String[] args) {
//        MvpAutomaticCreation dialog = new MvpAutomaticCreation();
//        dialog.pack();
//        dialog.setVisible(true);
//        System.exit(0);
//    }

    public interface DialogCallBack {
        void ok(String moduleName, String moduleNameChinese, int type,String appName);
    }

}
