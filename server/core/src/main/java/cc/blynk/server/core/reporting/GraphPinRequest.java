package cc.blynk.server.core.reporting;

import cc.blynk.server.core.model.DataStream;
import cc.blynk.server.core.model.enums.PinType;
import cc.blynk.server.core.model.widgets.outputs.graph.AggregationFunctionType;
import cc.blynk.server.core.model.widgets.outputs.graph.GraphGranularityType;
import cc.blynk.server.core.model.widgets.outputs.graph.Period;

import java.util.Arrays;

import static cc.blynk.server.core.model.widgets.outputs.graph.Period.LIVE;
import static cc.blynk.utils.IntArray.EMPTY_INTS;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 23.10.15.
 */
public class GraphPinRequest {

    public final int dashId;

    public final int deviceId;

    public final int[] deviceIds;

    public final boolean isTag;

    public final PinType pinType;

    public final short pin;

    private final Period graphPeriod;

    public final AggregationFunctionType functionType;

    public final int count;

    public final GraphGranularityType type;

    public final int skipCount;

    public long from;

    public long to;

    public GraphPinRequest(int dashId, int deviceId, DataStream dataStream,
                           Period period, int skipCount, AggregationFunctionType function) {
        this.dashId = dashId;
        this.deviceId = deviceId;
        this.deviceIds = EMPTY_INTS;
        this.isTag = false;
        if (dataStream == null) {
            this.pinType = PinType.VIRTUAL;
            this.pin = (short) DataStream.NO_PIN;
        } else {
            this.pinType = (dataStream.pinType == null ? PinType.VIRTUAL : dataStream.pinType);
            this.pin = dataStream.pin;
        }
        this.graphPeriod = period;
        this.functionType = (function == null ? AggregationFunctionType.AVG : function);
        this.count = period.numberOfPoints;
        this.type = period.granularityType;
        this.skipCount = skipCount;
    }

    public boolean isLiveData() {
        return graphPeriod == LIVE;
    }

    public boolean isValid() {
        return deviceId != -1 || deviceIds.length > 0;
    }

    @Override
    public String toString() {
        return "GraphPinRequest{"
                + "dashId=" + dashId
                + ", deviceId=" + deviceId
                + ", deviceIds=" + Arrays.toString(deviceIds)
                + ", isTag=" + isTag
                + ", pinType=" + pinType
                + ", pin=" + pin
                + ", graphPeriod=" + graphPeriod
                + ", functionType=" + functionType
                + ", count=" + count
                + ", type=" + type
                + ", skipCount=" + skipCount
                + '}';
    }
}
