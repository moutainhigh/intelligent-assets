package com.dili.ia.provider;

import com.dili.ia.glossary.AssetsTypeEnum;
import com.dili.ia.glossary.TransactionItemTypeEnum;
import com.dili.ss.metadata.FieldMeta;
import com.dili.ss.metadata.ValuePair;
import com.dili.ss.metadata.ValuePairImpl;
import com.dili.ss.metadata.ValueProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <B>Description</B>
 * 本软件源代码版权归农丰时代及其团队所有,未经许可不得任意复制与传播
 * <B>农丰时代科技有限公司</B>
 *
 * @author qinkelan
 * @createTime 2020-03-11 18:13
 */
@Component
public class AssetsTypeProvider implements ValueProvider {
    private static final List<ValuePair<?>> BUFFER = new ArrayList<>();
    // 杭州二期 资产类型只展示 【摊位】【其它】
    static {
        AssetsTypeEnum arr[] = new AssetsTypeEnum[2];
        arr[0] = AssetsTypeEnum.BOOTH;
        arr[1] = AssetsTypeEnum.OTHER;

        BUFFER.addAll(Stream.of(arr)
                .map(e -> new ValuePairImpl<>(e.getName(), e.getCode().toString()))
                .collect(Collectors.toList()));
    }

    @Override
    public List<ValuePair<?>> getLookupList(Object o, Map map, FieldMeta fieldMeta) {
        return BUFFER;
    }

    @Override
    public String getDisplayText(Object object, Map map, FieldMeta fieldMeta) {
        if (null == object) {
            return null;
        }
        ValuePair<?> valuePair = BUFFER.stream().filter(val -> object.toString().equals(val.getValue())).findFirst().orElseGet(null);
        if (null != valuePair) {
            return valuePair.getText();
        }
        return null;
    }
}
