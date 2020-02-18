package com.dili.ia.provider;

/**
 * <B>Description</B>
 * 本软件源代码版权归农丰时代及其团队所有,未经许可不得任意复制与传播
 * <B>农丰时代科技有限公司</B>
 *
 * @author jcy
 * @createTime 2018/11/1 17:17
 */

import com.dili.assets.sdk.dto.CategoryDTO;
import com.dili.ia.rpc.CategoryRpc;
import com.dili.ss.metadata.FieldMeta;
import com.dili.ss.metadata.ValuePair;
import com.dili.ss.metadata.ValuePairImpl;
import com.dili.ss.metadata.ValueProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 城市提供者
 * @author asiamaster
 */
@Component
public class CategoryProvider implements ValueProvider {

    @Autowired
    private CategoryRpc categoryRpc;

    @Override
    public List<ValuePair<?>> getLookupList(Object val, Map metaMap, FieldMeta fieldMeta) {
//        CategoryDTO categoryDTO = new CategoryDTO();
//        if(null == val){
//            categoryDTO.setParent(0L);
//        }else{
//            categoryInput.setKeyword(val.toString());
//        }
//        List<CategoryDTO> categoryList = categoryRpc.list(categoryDTO).getData();

        List<CategoryDTO> categoryList = new ArrayList<>();
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(1L);
        categoryDTO.setName("蔬菜");
        categoryList.add(categoryDTO);

        List<ValuePair<?>> buffer = new ArrayList<ValuePair<?>>();
        categoryList.forEach(o->{
            buffer.add(new ValuePairImpl(o.getName(),o.getId().toString()));
        });
        return buffer;
    }

    @Override
    public String getDisplayText(Object obj, Map metaMap, FieldMeta fieldMeta) {
        return null;
    }
}