package com.dili.ia.provider;

/**
 * <B>Description</B>
 * 本软件源代码版权归农丰时代及其团队所有,未经许可不得任意复制与传播
 * <B>农丰时代科技有限公司</B>
 *
 * @author jcy
 * @createTime 2018/11/1 17:17
 */

import com.dili.ss.dto.DTOUtils;
import com.dili.ss.metadata.FieldMeta;
import com.dili.ss.metadata.ValuePair;
import com.dili.ss.metadata.ValuePairImpl;
import com.dili.ss.metadata.ValueProvider;
import com.dili.uap.sdk.domain.Department;
import com.dili.uap.sdk.domain.dto.DepartmentDto;
import com.dili.uap.sdk.glossary.DataAuthType;
import com.dili.uap.sdk.rpc.DepartmentRpc;
import com.dili.uap.sdk.session.SessionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 城市提供者
 * @author asiamaster
 */
@Component
public class AuthDepartmentProvider implements ValueProvider {
    @Autowired
    private DepartmentRpc departmentRpc;

    @Override
    public List<ValuePair<?>> getLookupList(Object val, Map metaMap, FieldMeta fieldMeta) {
        SessionContext sessionContext = SessionContext.getSessionContext();
        //获取指定类型的数据权限
        List<Map> typeDataAuthes = sessionContext.dataAuth(DataAuthType.DEPARTMENT.getCode());
        List<String> depIds = typeDataAuthes.stream().map(o->(String.valueOf(o.get("value")))).collect(Collectors.toList());
        DepartmentDto department = DTOUtils.newInstance(DepartmentDto.class);
        department.setIds(depIds);
        List<Department> departments = departmentRpc.listByExample(department).getData();
        List<ValuePair<?>> buffer = new ArrayList<ValuePair<?>>();
        departments.forEach(o->{
            buffer.add(new ValuePairImpl(o.getName(),o.getId()));
        });
        return buffer;
    }

    @Override
    public String getDisplayText(Object obj, Map metaMap, FieldMeta fieldMeta) {
        return null;
    }
}
