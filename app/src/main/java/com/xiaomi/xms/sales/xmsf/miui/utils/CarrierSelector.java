
package com.xiaomi.xms.sales.xmsf.miui.utils;

import java.util.HashMap;
import java.util.Map;

public final class CarrierSelector<T> {

    private CARRIER mDefaultCarrier;

    private final Map<CARRIER, T> mCarrierMap = new HashMap<CARRIER, T>();

    public static enum CARRIER {
        CHINA_MOBILE,
        CHINA_UNICOM,
        CHINA_TELECOM,
    }

    public CarrierSelector() {
    }

    public CarrierSelector(CARRIER defaultCarrier) {
        mDefaultCarrier = defaultCarrier;
    }

    public void register(CARRIER carrier, T value) {
        if (carrier == null) {
            throw new IllegalArgumentException("carrier not nullable");
        }
        mCarrierMap.put(carrier, value);
    }

    /**
     * Get the carrier according to the provided mcc-mnc
     *
     * @param mccMnc mcc-mnc value
     * @return one of {@link CARRIER} values, or null if the mcc mnc is not
     *         registered.
     */
    public CARRIER selectCarrier(String mccMnc) {
        return internalSelectCarrier(mccMnc, null);
    }

    /**
     * Get the registered value according to the provided mcc-mnc
     *
     * @param mccMnc mcc-mnc value
     * @return the registered value if the provided mcc-mnc is registered by
     *         {@link #register(CarrierSelector.CARRIER, Object)}. Otherwise, if
     *         the mcc-mnc is not found, use the default carrier specified in
     *         the constructor
     */
    public T selectValue(String mccMnc) {
        CARRIER carrier = internalSelectCarrier(mccMnc, mDefaultCarrier);
        return mCarrierMap.get(carrier);
    }

    /**
     * Get the registered value according to the provided mcc-mnc
     *
     * @param mccMnc mcc-mnc value
     * @param useDefault whether use the default value if the provided mcc-mnc
     *            is not found
     * @return the registered value if the provided mcc-mnc is registered by
     *         {@link #register(CarrierSelector.CARRIER, Object)}. If the
     *         mcc-mnc is not found and {@code useDefault} is true, the default
     *         value is returned. Otherwise returns null.
     */
    public T selectValue(String mccMnc, boolean useDefault) {
        CARRIER carrier = internalSelectCarrier(mccMnc,
                useDefault ? mDefaultCarrier : null);
        return mCarrierMap.get(carrier);
    }

    /*
     *  <item>"46000"</item> => <item>"46000"</item>
     *  <item>"46001"</item> => <item>"46001"</item>
     *  <item>"46002"</item> => <item>"46000"</item>
     *  <item>"46003"</item> => <item>"46003"</item>
     *  <item>"46005"</item> => <item>"46003"</item>
     *  <item>"46006"</item> => <item>"46001"</item>
     *  <item>"46007"</item> => <item>"46000"</item>
     *  <item>"46009"</item> => <item>"46003"</item>
     *  <item>"46601"</item> => <item>"46601"</item>
     *  <item>"46605"</item> => <item>"46605"</item>
     *  <item>"46689"</item> => <item>"46689"</item>
     *  <item>"46692"</item> => <item>"46692"</item>
     *  <item>"46697"</item> => <item>"46697"</item>
     */
    private String getOperatorNumeric(String mccMnc) {
        HashMap<String, String> operatorMap = new HashMap<String, String>();
        operatorMap.put("46000", "46000");
        operatorMap.put("46001", "46001");
        operatorMap.put("46002", "46000");
        operatorMap.put("46003", "46003");
        operatorMap.put("46005", "46003");
        operatorMap.put("46006", "46001");
        operatorMap.put("46007", "46000");
        operatorMap.put("46009", "46003");

        if (operatorMap.containsKey(mccMnc)) {
            return operatorMap.get(mccMnc);
        }
        return mccMnc;
    }

    public static final String EQ_OPERATOR_CM = "46000";

    public static final String EQ_OPERATOR_CU = "46001";

    public static final String EQ_OPERATOR_CT = "46003";

    private CARRIER internalSelectCarrier(String mccMnc,
            CARRIER defaultCarrier) {
        CARRIER carrier = defaultCarrier;

        final String eqOperator = getOperatorNumeric(mccMnc);
        if (EQ_OPERATOR_CM.equals(eqOperator)) {
            carrier =
                    CARRIER.CHINA_MOBILE;
        } else if (EQ_OPERATOR_CU.equals(eqOperator)) {
            carrier =
                    CARRIER.CHINA_UNICOM;
        } else if (EQ_OPERATOR_CT.equals(eqOperator)) {
            carrier =
                    CARRIER.CHINA_TELECOM;
        }

        return carrier;
    }

}
