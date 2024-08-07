package cn.sourceplan.quality.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.sourceplan.common.annotation.Excel;
import cn.sourceplan.common.core.domain.BaseEntity;

/**
 * 质检原因对象 qc_reason
 *
 * @author jinzhong
 * @date 2023-02-21
 */
@Data
@TableName("qc_reason")
public class Reason extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 编号
     */
    @Excel(name = "编号")
    private String number;

    /**
     * 名称
     */
    @Excel(name = "名称")
    private String name;

    /**
     * 类型
     */
    @Excel(name = "类型")
    private String type;

    /**
     * 原因类别
     */
    @Excel(name = "原因类别")
    private String qcReasonClass;

    /**
     * 状态
     */
    @Excel(name = "状态")
    private String status;

}