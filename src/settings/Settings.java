package settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import compute.AppNames;
import dialog.DialogAppName;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Settings implements Configurable {

    // region view
    private JButton btnAdd;
    private JButton btnDel;
    private JTable table1;
    private JPanel mMainPanel;
    private JPanel panel1;
    private JButton button1;
    // endregion

    // region 表格有关参数
    private DefaultTableModel mDefaultTableModel;
    private int mCurRow = -1; // 当前行
    private String mCurAppName = null; // 当前选择的app名称
    // endregion

    // region 变量
    private List<String> mAppNameDels; // 缓存的删除数据源
    private boolean mIsModify = false; // 是否已经修改
    // endregion


    /**
     * 在settings中显示的名称 2017/3/20 14:12
     *
     * @return 名称
     */
    @Nls
    @Override
    public String getDisplayName() {
        return "MvpAutomaticCreation";
    }

    /**
     * 初始化控件 2017/3/20 14:19
     *
     * @return
     */
    @Nullable
    @Override
    public JComponent createComponent() {
        this.mAppNameDels = new ArrayList<>();

        // 删除事件
        btnDel.addActionListener(e -> {
            mAppNameDels.add(mCurAppName);
            mIsModify = true;

            // 刷新界面
            mDefaultTableModel.removeRow(mCurRow);

            // 删除按钮禁止点击
            btnDel.setEnabled(false);
            mCurRow = -1;

        });

        // 添加事件
        btnAdd.addActionListener(e -> {
            // 添加前，先保存当前环境
            mIsModify = false; // 标志修改完成
            AppNames.removeData(mAppNameDels);
            DialogAppName dialogAppName = new DialogAppName(content -> {

                // 刷新界面
                refresh();
            });

            dialogAppName.setVisible(true);
        });


        return mMainPanel;
    }

    /**
     * 是否修改
     *
     * @return true 激活apply按钮
     */
    @Override
    public boolean isModified() {
        return this.mIsModify;
    }

    /**
     * 点击【apply】、【OK】时，调用
     *
     * @throws ConfigurationException
     */
    @Override
    public void apply() throws ConfigurationException {
        if (this.mIsModify) {
            this.mIsModify = false; // 标志修改完成
            AppNames.removeData(mAppNameDels);
        }
    }

    /**
     * 点击【Reset】时，调用
     */
    @Override
    public void reset() {
        // 重置数据
        if (this.mIsModify) {
            this.refresh();
        }
    }

    /**
     * 刷新
     */
    private void refresh() {
        this.mIsModify = false;

        // 重新初始化
        this.initTableData(mDefaultTableModel);

        // 有多少列，setPreferredWidth都得设置，才能生效
        table1.getColumnModel().getColumn(0).setPreferredWidth(465);
        table1.setRowHeight(25);
    }

    /**
     * 初始化表格数据
     */
    private void initTableData(DefaultTableModel tableModel) {

        AppNames.initData();

        if (AppNames.mapData.size() == 0)
            return;

        Object[][] object = new Object[AppNames.mapData.size()][1];
        int i = 0;

        for (String s : AppNames.mapData.keySet()) {
            object[i][0] = AppNames.mapData.get(s).get(AppNames.APPNAME);
            i++;
        }

        tableModel.setDataVector(object, new Object[]{AppNames.APPNAME});
    }


    /**
     * 这个要写，不写不知道为什么会不显示
     */
    private void createUIComponents() {
        mDefaultTableModel = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return super.getColumnClass(columnIndex);
            }
        };

        // 初始化数据
        initTableData(mDefaultTableModel);
        table1 = new JTable(mDefaultTableModel) {
            public void tableChanged(TableModelEvent e) {
                super.tableChanged(e);
                repaint();
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }


        };

        table1.getTableHeader().setPreferredSize(new Dimension(table1.getTableHeader().getWidth(), 35));

        // 添加点击事件
        table1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 1) { // 获取点击的复选框
                    int columnIndex = table1.columnAtPoint(e.getPoint()); //获取点击的列
                    int rowIndex = table1.rowAtPoint(e.getPoint()); //获取点击的行
                    mCurRow = rowIndex;
                    if (columnIndex == 0) {
                        mIsModify = false; // 标志修改，激活apply
                        String appName = (String) table1.getValueAt(rowIndex, 0);
                    }

                    // 判断是否允许删除
                    if (mCurRow >= 0) {
                        mCurAppName = (String) table1.getValueAt(rowIndex, 0);
                        btnDel.setEnabled(true);
                    } else {
                        btnDel.setEnabled(false);
                    }
                }
            }
        });


        // 有多少列，setPreferredWidth都得设置，才能生效
        table1.getColumnModel().getColumn(0).setPreferredWidth(465);
        table1.setRowHeight(25);

        table1.setRowHeight(25);
    }

    /**
     * 一定要实现，否则在Android Studio中会报错
     */
    @Override
    public void disposeUIResources() {

    }

}
