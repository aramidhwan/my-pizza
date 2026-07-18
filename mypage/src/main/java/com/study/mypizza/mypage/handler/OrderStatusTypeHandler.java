package com.study.mypizza.mypage.handler;

import com.study.mypizza.mypage.enums.OrderStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(OrderStatus.class)
public class OrderStatusTypeHandler extends BaseTypeHandler<OrderStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i,
                                    OrderStatus parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, parameter.getStatus());
    }

    @Override
    public OrderStatus getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        return OrderStatus.fromStatus(rs.getString(columnName));
    }

    @Override
    public OrderStatus getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        return OrderStatus.fromStatus(rs.getString(columnIndex));
    }

    @Override
    public OrderStatus getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        return OrderStatus.fromStatus(cs.getString(columnIndex));
    }
}
