package com.geekq.admin.query;

import com.geekq.miaosha.utils.DateUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * IpLog查询对象
 * @author 邱润泽
 *
 */
@Setter
@Getter
public class IpLogQueryObject extends QueryObject {

	private Date beginDate;
	private Date endDate;
	private String username;
	private int userType=-1;
	private boolean like;
	private int state=-1;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getEndDate() {
		if (endDate != null) {
			return DateUtil.endOfDay(endDate);
		}
		return null;
	}

}
