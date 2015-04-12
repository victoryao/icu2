
package com.xiaomi.xms.sales.util;

import java.util.HashSet;
import java.util.Set;

import android.os.Environment;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.request.HostManager;

public final class Constants {
    public static final int DAY_IN_MILLIS = 24 * 60 * 60 * 1000;

    public static final int UNINITIALIZED_NUM = -1;

    public final static String serviceNumberCache = "serviceNumberCache";
    public final static String normalServiceNumberCache = "normalOrderIds";
    public final static String productCache = "prodCc";
    public final static String productSnCache = "prodSnCc";
    public final static String posHistoryCache = "posHistoryCache";
    //public final static String snCache = "SNCc";
    public final static String modify = "modify";
    public final static String xianhuo = "xianhuo";
    public final static int xianhuo_orderType = 2;
    public final static String serviceNumber ="serviceNumber";
    public final static int POS_PAY = 100;
    public final static int CASH_PAY = 101;
    public final static String PRINTER = "PRINTER";  //打印机标示
    public final static String POSDISPLAYNAME = "pos";  //搜索POS蓝牙设备包含名字
    
    public final static int SN_LENGTH = 12; //SN长度
    public final static int SKU_LENGTH = 4; //SKU长度
    public final static int NEW_SN_LENGTH = 14;  //新sn的长度
    public final static int DUOKAN_NEW_SN_LENGTH = 18;  //多看新sn的长度
    public final static int NEW_SKU_LENGTH = 5; //新sku长度
    public final static int REFUND_DAY = 7;  //退款间隔天数
    public final static String POS_PAY_NAME = "易宝POS";  
    public final static String CASH_PAY_NAME = "现金";
    public static final class AppUpdate {
        public static final String FILE_PREFIX_FILE = "file://";
        public static final String FILE_SUFFIX_APK = ".apk";
        public static final String FILE_TYPE_APK = "application/vnd.android.package-archive";
        public static final String PREF_LAST_UPDATE_IS_OK = "pref_last_update_is_ok";
        public static final String PREF_LAST_CHECK_UPDATE = "pref_last_check_update";
        public static final int PERIOD_UPDATE_OK = 2 * 60 * 60 * 1000;
        public static final int PERIOD_CHECK_UPDATE = 10 * 60 * 1000;
        public static final String PREF_DOWNLOAD_ID = "pref_download_id";

        public static final String VALUE_TYPE_FORCE_IN_WIFI = "wifiForce";
        public static final String VALUE_TYPE_FORCE = "force";
    }
    
    public static final class LogType {
        public static final String CONNECTING_POS_DEVICE = "CONNECTING_POS_DEVICE";
        public static final String START_BLUETOOTH_DISCOVERY = "START_BLUETOOTH_DISCOVERY";
        public static final String START_BLUETOOTH_DISCOVERY_FINISH = "START_BLUETOOTH_DISCOVERY_FINISH";
        public static final String START_CONNECTING_BLUETOOTH_DEVICE = "START_CONNECTING_BLUETOOTH_DEVICE";
        public static final String CONNECTING_BLUETOOTH_DEVICE_LOST = "CONNECTING_BLUETOOTH_DEVICE_LOST";
        public static final String CONNECTING_BLUETOOTH_DEVICE_FAILED = "CONNECTING_BLUETOOTH_DEVICE_FAILED";
        public static final String CONNECTING_BLUETOOTH_DEVICE_END = "CONNECTING_BLUETOOTH_DEVICE_END";
        public static final String CONNECTING_BLUETOOTH_DEVICE_SUCCESS = "CONNECTING_BLUETOOTH_DEVICE_SUCCESS";
        public static final String PACKED_INFO_LOADER_BEGIN = "PACKED_INFO_LOADER_BEGIN";
        public static final String PACKED_INFO_LOADER_END = "PACKED_INFO_LOADER_END";
        public static final String ADD_PAYINTO_BEDIN = "ADD_PAYINTO_BEDIN";
        public static final String ADD_PAYINTO_END = "ADD_PAYINTO_END";
        public static final String BIZ_SIGNIN = "BIZ_SIGNIN";
        public static final String PAY_CHECK_START = "PAY_CHECK_START";
        public static final String PAY_CHECK_END = "PAY_CHECK_END";
        public static final String BIZ_CONSUME = "BIZ_CONSUME";
        public static final String BIZ_CONSUME_REVERSE = "BIZ_CONSUME_REVERSE";
        public static final String POS_ONCONNECT_LOST = "POS_ONCONNECT_LOST";
        public static final String ON_INPUTPIN = "ON_INPUTPIN";
        public static final String ON_SWIPECARD = "ON_SWIPECARD";
        public static final String PRINT_START = "PRINT_START";
        public static final String PRINT_END = "PRINT_END";
        public static final String XIANHUO_SCANNER_START = "XIANHUO_SCANNER_START";
        public static final String XIANHUO_SCANNER_END = "XIANHUO_SCANNER_END";
        public static final String BACK_UP_POS_INFO = "BACK_UP_POS_INFO";
        public static final String CONSUME_SUCCESS = "CONSUME_SUCCESS";
        public static final String CONSUME_RESULT_FALSE = "CONSUME_RESULT_FALSE";
        
    }

    public static final class WebView {
        public static final String EVENT_LOGIN = "login";
        public static final String EVENT_PRODUCT = "product";
        public static final String EVENT_SHOPPING = "shopping";
        public static final String EVENT_NEW_WEB = "opennew";
        public static final String EVENT_GO_HOME = "gohome";
        public static final String EVENT_FCODE = "fcode";
        public static final String EVENT_ORDERLIST = "orderlist";
        public static final String EVENT_CHECK_INSTALL = "installed";
        public static final String EVENT_CHECK_UPDATE = "checkupdate";
        public static final String EVENT_IS_WIFI = "iswifi";
        public static final String EVENT_BOTTOM_STYLE = "bottomstyle";
        public static final String EVENT_PRODUCTLIST = "productlist";
        public static final String EVENT_WEIBO = "weibo";
        public static final String EVENT_MIPHONE_DETAIL = "miphonedetail";
        public static final String EVENT_SHOW_TITLE_BAR = "showtitlebar";
        public static final String EVENT_HIDE_TITLE_BAR = "hidetitlebar";
        public static final String EVENT_CALLS_RECHARGE = "callsrecharge";
        public static final String EVENT_GO_COUPON = "gocoupon";

        public static final String VALUE_NULL = "null";
    }

    public static final class Intent {
        // Intent Extra
        public static final String EXTRA_CATEGORY_ID = "com.xiaomi.xms.sales.extra_categoryid";
        public static final String EXTRA_CATEGORY_NAME = "com.xiaomi.xms.sales.extra_category_name";
        public static final String EXTRA_CARTLIST_ITEM_ID = "com.xiaomi.xms.sales.extra_cartlist_item_id";
        public static final String EXTRA_CATEGORY_DATA_TYPE = "com.xiaomi.xms.sales.extra_category_data_type";

        public static final String EXTRA_ERROR_SN = "com.xiaomi.xms.sales.extra_error_sn";
        public static final String EXTRA_PRODUCT_ID = "com.xiaomi.xms.sales.extra_product_id";
        public static final String EXTRA_CONTAIN_ID = "com.xiaomi.xms.sales.extra_contain_id";
        public static final String EXTRA_P_ID = "com.xiaomi.xms.sales.extra_p_id";
        public static final String EXTRA_PRODUCTVIEW_POSITION = "com.xiaomi.xms.sales.extra_productview_postion";
        public static final String EXTRA_COMPAIGN_URL = "com.xiaomi.xms.sales.extra_compaign_url";
        public static final String EXTRA_ABOUT_URL = "com.xiaomi.xms.sales.extra_about_url";
        public static final String EXTRA_IS_MIPHONE = "com.xiaomi.xms.sales.extra_is_miphone";
        public static final String EXTRA_MIPHONE_NAME = "com.xiaomi.xms.sales.extra_miphone_name";
        public static final String EXTRA_SHOPPING_COUNT = "com.xiaomi.xms.sales.extra_shopping_count";
        public static final String EXTRA_MIHOME_SHOPPING_COUNT = "com.xiaomi.xms.sales.extra_mihome_shopping_count";
        public static final String EXTRA_ADDRESS_ID = "com.xiaomi.xms.sales.extra_address_id";
        public static final String EXTRA_DEL_ADDRESS_RESULT = "com.xiaomi.xms.sales.extra_del_address_result";
        public static final String EXTRA_DEL_ADDRESS_RESULT_MSG = "com.xiaomi.xms.sales.extra_del_address_result_msg";
        public static final String EXTRA_PAYMENT_URL = "com.xiaomi.ship.extra_payment_url";
        public static final String EXTRA_PAYMENT_ORDER_ID = "com.xiaomi.xms.sales.extra_payment_order_id";
        public static final String EXTRA_ORDER_TYPE = "com.xiaomi.xms.sales.extra_order_type";   //订单类型
        public static final String EXTRA_ONLY_VIEW = "com.xiaomi.xms.sales.extra_only_view"; //只是察看订单信息
        public static final String EXTRA_PRINTER_REDO ="com.xiaomi.xms.sales.extra_printer_redo";  //再次选择打印机
        public static final String EXTRA_PAYMENT_ERROR_INFO = "com.xiaomi.xms.sales.extra_payment_error_info";
        public static final String EXTRA_PAYMENT_POS_SUCCESS_INFO = "com.xiaomi.xms.sales.extra_payment_pos_success_info";
        public static final String EXTRA_PAYMENT_POS_BLUETOOTH_NAME = "com.xiaomi.xms.sales.extra_payment_pos_bluetooth_name";
        public static final String EXTRA_PAYMENT_ORDER_TOTAL_PRICE = "com.xiaomi.xms.sales.extra_payment_order_total_price";
        public static final String EXTRA_PAYMENT_ORDER_ADD_TIME = "com.xiaomi.xms.sales.extra_payment_order_add_time";
        public static final String EXTRA_EDIT_ORDER_MESSAGE_CHECK = "com.xiaomi.xms.sales.extra_edit_order_message_check";
        public static final String EXTRA_PAYMENT_SECURITY_KEY = "com.xiaomi.xms.sales.extra_payment_security_key";
        public static final String EXTRA_PAYMENT_ORDER_TYPE = "com.xiaomi.xms.sales.extra_payment_order_type";
        public static final String EXTRA_PAYMENT_SERVICE_ORDER = "com.xiaomi.xms.sales.extra_payment_service_order";
        public static final String EXTRA_PAYMENT_FEE = "com.xiaomi.xms.sales.extra_payment_fee";
        public static final String EXTRA_PAYMENT_TYPE = "com.xiaomi.xms.sales.extra_online_payment_type";
        public static final String EXTRA_PAYMENT_USER_CONFIRM_RESULT = "com.xiaomi.xms.sales.extra_payment_user_confirm_result";
        public static final String EXTRA_ADDRESS_CONSIGNEE = "com.xiaomi.xms.sales.extra_address_consignee";
        public static final String EXTRA_ADDRESS_PROVINCE = "com.xiaomi.xms.sales.extra_address_province";
        public static final String EXTRA_ADDRESS_CITY = "com.xiaomi.xms.sales.extra_address_city";
        public static final String EXTRA_ADDRESS_DISTRICT = "com.xiaomi.xms.sales.extra_address_district";
        public static final String EXTRA_ADDRESS_LOCATION = "com.xiaomi.xms.sales.extra_address_location";
        public static final String EXTRA_ADDRESS_ZIPCODE = "com.xiaomi.xms.sales.extra_address_zipcode";
        public static final String EXTRA_ADDRESS_TEL = "com.xiaomi.xms.sales.extra_address_tel";
        public static final String EXTRA_ADDRESS_RESULT = "com.xiaomi.xms.sales.extra_del_address_result";
        public static final String EXTRA_ADDRESS_NEWID = "com.xiaomi.xms.sales.extra_add_address_new_id";
        public static final String EXTRA_ADDRESS_RESULT_MSG = "com.xiaomi.xms.sales.extra_del_address_result_msg";
        public static final String EXTRA_ADDRESS_RESULT_CODE = "com.xiaomi.xms.sales.extra_address_result_code";
        public static final String EXTRA_ORDER_LIST_TYPE = "com.xiaomi.xms.sales.extra_order_list_type";
        public static final String EXTRA_ORDER_LIST_TYPE_IS_SHOW_NON_PAYMENT = "com.xiaomi.xms.sales.extra_order_list_type_is_show_non_payment";
        public static final String EXTRA_ORDER_LIST_TYPE_EXPRESS = "com.xiaomi.xms.sales.extra_order_list_type_express";
        public static final String EXTRA_ADD_SHOPPING_CART_RESULT_MSG = "com.xiaomi.xms.sales.extra_del_add_shopping_cart_result_msg";
        public static final String EXTRA_ACTIVITY_URL = "com.xiaomi.xms.sales.extra_activity_url";
        public static final String EXTRA_ACTIVITY_VERSION = "com.xiaomi.xms.sales.extra_activity_version";
        public static final String EXTRA_ACTIVITY_TYPE = "com.xiaomi.xms.sales.extra_activity_type";
        public static final String EXTRA_UPDATE_URL = "com.xiaomi.xms.sales.extra_update_url";
        public static final String EXTRA_UPDATE_VERSION = "com.xiaomi.xms.sales.extra_update_version";
        public static final String EXTRA_UPDATE_TYPE = "com.xiaomi.xms.sales.extra_update_type";
        public static final String EXTRA_UPDATE_VERSION_NAME = "com.xiaomi.idea.extra_update_version_name";
        public static final String EXTRA_UPDATE_SUMMARY = "com.xiaomi.idea.extra_update_summary";
        public static final String EXTRA_RESULT = "com.xiaomi.xms.sales.result";
        public static final String EXTRA_GO_TO_FRAGMENT = "com.xiaomi.xms.sales.extra_go_to_fragment";
        public static final String EXTRA_ENTER_NFC_TYPE = "com.xiaomi.xms.sales.extra_enter_nfc_type";

        public static final String EXTRA_SHOP_INTENT_SERVICE_ACTION = "com.xiaomi.xms.sales.extra_shop_intent_service_action";
        public static final String EXTRA_SHOP_INTENT_SERVICE_ACTION_RESULT = "com.xiaomi.xms.sales.extra_submit_result";
        public static final String EXTRA_SHOP_INTENT_SERVICE_RETURN_JSON = "com.xiaomi.xms.sales.extra_submit_json";
        public static final String EXTRA_ORDER_SUBMIT_ORDER_ID = "com.xiaomi.xms.sales.extra_order_submit_order_id";
        public static final String EXTRA_FULL_SCREEN_START_INDEX = "com.xiaomi.xms.sales.extra_full_screen_start_index";
        public static final String EXTRA_ORDER_EXPRESS = "com.xiaomi.xms.sales.extra_order_express";
        public static final String EXTRA_CHECKCODE_URL = "com.xiaomi.xms.sales.extra_checkcode_url";
        public static final String EXTRA_CHECKCODE_VCODE = "com.xiaomi.xms.sales.extra_checkcode_vcode";
        public static final String EXTRA_CHECKCODE_RESULT = "com.xiaomi.xms.sales.extra_checkcode_result";
        public static final String EXTRA_CHECKCODE_MESSAGE = "com.xiaomi.xms.sales.extra_checkcode_message";
        public static final String EXTRA_CHECKCODE_FCODE = "com.xiaomi.xms.sales.extra_checkcode_fcode";
        public static final String EXTRA_CHECKCODE_AUTHCODE = "com.xiaomi.xms.sales.extra_checkcode_authcode";
        public static final String EXTRA_CHECKCODE_PRODUCTID = "com.xiaomi.xms.sales.extra_checkcode_id";
        public static final String EXTRA_CHECKCODE_LISTSTR = "com.xiaomi.xms.sales.extra_checkcode_list";
        public static final String EXTRA_COUPON_ID = "com.xiaomi.xms.sales.extra_coupon_id";
        public static final String EXTRA_COMBO_TYPE = "com.xiaomi.xms.sales.extra_combo_type";
        public static final String EXTRA_PRODUCT_INFO = "com.xiaomi.xms.sales.extra_product_info";
        public static final String EXTRA_PRODUCT_NAME = "com.xiaomi.xms.sales.extra_product_name";
        public static final String EXTRA_SECURITY_CODE = "com.xiaomi.xms.sales.extra_security_code";
        public static final String EXTRA_REMAIN_NUMBER = "com.xiaomi.xms.sales.extra_remain_number";
        public static final String EXTRA_PRICE = "com.xiaomi.xms.sales.extra_price";
        public static final String EXTRA_MARKET_PRICE = "com.xiaomi.xms.sales.extra_market_price";
        public static final String EXTRA_IMAGE_URL = "com.xiaomi.xms.sales.extra_image_url";
        public static final String EXTRA_MIHOME_ID = "com.xiaomi.xms.sales.extra_mihome_id";
        public static final String EXTRA_MIHOME_BUY = "com.xiaomi.xms.sales.extra_mihome_buy";
        public static final String EXTRA_MIHOME_NAME = "com.xiaomi.xms.sales.extra_mihome_name";
        public static final String EXTRA_MIHOME_DATE_DAY = "com.xiaomi.xms.sales.extra_mihome_date_day";
        public static final String EXTRA_MIHOME_DATE_HOURS = "com.xiaomi.xms.sales.extra_mihome_date_hours";
        public static final String EXTRA_STATION_PROVINCE_NAME_EN = "com.xiaomi.xms.sales.extra_station_province_name_en";
        public static final String EXTRA_STATION_PROVINCE_NAME_CN = "com.xiaomi.xms.sales.extra_station_province_name_cn";
        public static final String EXTRA_ORDER_DELIVER_TIME = "com.xiaomi.xms.sales.extra_order_deliver_time";
        public static final String EXTRA_ORDER_HAS_PHONE = "com.xiaomi.xms.sales.extra_order_has_phone";
        public static final String EXTRA_ORDER_EDIT_TYPE = "com.xiaomi.xms.sales.extra_order_edit_type";
        public static final String EXTRA_ORDER_EDIT_OLDTEL = "com.xiaomi.xms.sales.extra_order_edit_oldtel";
        public static final String EXTRA_MIHOME_RESERVE_ID = "com.xiaomi.xms.sales.extra_mihome_reserve_id";
        public static final String EXTRA_INCAST_PRODUCTS = "com.xiaomi.xms.sales.incast_products";
        public static final String EXTRA_CAMPAIGN_SHOW_TITLE = "com.xiaomi.xms.sales.extra_campaign_show_title";
        public static final String EXTRA_CAMPAIGN_SHOW_BOTTOM = "com.xiaomi.xms.sales.extra_campaign_show_bottom";
        public static final String EXTRA_CAMPAIGN_FINISH_ANIM = "com.xiaomi.xms.sales.extra_campaign_finish_anim";
        public static final String EXTRA_CAMPAIGN_FROM_PUSH = "com.xiaomi.xms.sales.extra_campaign_from_push";
        public static final String EXTRA_MIHOME_ERROR_RESULT = "com.xiaomi.xms.sales.extra_mihome_error_result";
        public static final String EXTRA_RECHARGE_RECHARGE_NAME = "com.xiaomi.xms.sales.extra_recharge_recharge_name";
        public static final String EXTRA_RECHARGE_PERVALUE = "com.xiaomi.xms.sales.extra_recharge_pervalue";
        public static final String EXTRA_SEARCH_RESULT_KEYWORD = "com.xiaomi.xms.sales.extra_search_result_keyword";
        public static final String EXTRA_REVIEW_GOODS_NAME = "com.xiaomi.xms.sales.extra_review_goods_name";
        public static final String EXTRA_REVIEW_GOODS_ID = "com.xiaomi.xms.sales.extra_review_goods_id";
        public static final String EXTRA_REVIEW_QUALITY_RATE = "com.xiaomi.xms.sales.extra_review_quality_rate";
        public static final String EXTRA_REVIEW_SERVICE_RATE = "com.xiaomi.xms.sales.extra_review_service_rate";
        public static final String EXTRA_REVIEW_DELIVER_RATE = "com.xiaomi.xms.sales.extra_review_deliver_rate";
        public static final String EXTRA_REVIEW_CONTENT = "com.xiaomi.xms.sales.extra_review_content";
        public static final String EXTRA_ORDER_EDIT_ACTION = "com.xiaomi.xms.sales.extra_order_edit_action";
        public static final String EXTRA_NFC_TAG_ID = "com.xiaomi.xms.sales.extra_nfc_tag_id";
        public static final String EXTRA_NFC_WRITE = "com.xiaomi.xms.sales.extra_nfc_write";
        public static final String EXTRA_XIANHUO_SUBMIT = "com.xiaomi.xms.sales.xiaohuo_submit";
        //public static final String EXTRA_PRINTER_ID = "com.xiaomi.xms.sales.printerId";
        public static final String EXTRA_PRINTER_IP = "com.xiaomi.xms.sales.printerIP";
        public static final String EXTRA_REDUCE = "com.xiaomi.xms.sales.reduce";
        // Intent action
        public static final String ACTION_UPDATE_USER_INFO = "com.xiaomi.xms.sales.action_update_user_info";
        public static final String ACTION_DELETE_CARTITEM = "com.xiaomi.xms.sales.action_delete_cartitem";
        public static final String ACTION_UPDATE_SHOPPING_COUNT = "com.xiaomi.xms.sales.action_update_shopping_count";
        public static final String ACTION_UPDATE_MIHOME_SHOPPING_COUNT = "com.xiaomi.xms.sales.action_update_mihome_shopping_count";
        public static final String ACTION_CHECKOUT_SUBMIT = "com.xiaomi.xms.sales.action_checkout_submit";
        public static final String ACTION_UPDATE_PRODUCT_DETAIL = "com.xiaomi.xms.sales.action_update_product_detail";
        public static final String ACTION_DEL_ADDRESS = "com.xiaomi.xms.sales.del_address";
        public static final String ACTION_USE_ADDRESS = "com.xiaomi.xms.sales.use_address";
        public static final String ACTION_EDIT_ADDRESS = "com.xiaomi.xms.sales.edit_address";
        public static final String ACTION_ADD_ADDRESS = "com.xiaomi.xms.sales.add_address";
        public static final String ACTION_ORDER_SUBMIT = "com.xiaomi.xms.sales.action_order_submit";
        public static final String ACTION_XIANHUO_ORDER_SUBMIT = "com.xiaomi.xms.sales.action_xianhuo_order_submit";
        public static final String ACTION_EDIT_CONSUMPTION = "com.xiaomi.xms.sales.action_edit_consumption";
        public static final String ACTION_ORDER_LIST = "com.xiaomi.xms.sales.my_order_list";
        public static final String ACTION_ORDER_EDIT = "com.xiaomi.xms.sales.my_order_edit";
        public static final String ACTION_ADD_SHOPPING_CART = "com.xiaomi.xms.sales.action_add_shopping_cart";
        public static final String ACTION_MUTI_ADD_SHOPPING_CART = "com.xiaomi.xms.sales.action_muti_add_shopping_cart";
        public static final String ACTION_REPAIR_LIST = "com.xiaomi.xms.sales.my_repair_list";
        public static final String ACTION_VIEW_ORDER = "com.xiaomi.xms.sales.view_order";
        public static final String ACTION_VIEW_RECHARGE = "com.xiaomi.xms.sales.view.recharge";
        public static final String ACTION_VIEW_DELIVERORDER = "com.xiaomi.xms.sales.view.deliverorder";
        public static final String ACTION_VIEW_AFTER_SALE_ORDER = "com.xiaomi.xms.sales.view_after_sale_order";
        public static final String ACTION_CHECK_ACTIVITY = "com.xiaomi.xms.sales.action_check_activity";
        public static final String ACTION_CHECK_UPDATE = "com.xiaomi.xms.sales.action_check_update";
        public static final String ACTION_DOWNLOAD_COMPLETED = "com.xiaomi.xms.sales.action_download_completed";
        public static final String ACTION_CANCEL_ORDER = "com.xiaomi.xms.sales.action_cancel_order";
        public static final String ACTION_CANCEL_RECHARGE = "com.xiaomi.xms.sales.action_cancel_recharge";
        public static final String ACTION_SHOW_ACTIVITY = "com.xiaomi.xms.sales.action_show_activity";
        public static final String ACTION_ADD_PRODUCT_BY_NFC = "com.xiaomi.xms.sales.action_add_product_by_nfc";
        public static final String ACTION_WRITE_PRODUCT_TO_NFC = "com.xiaomi.xms.sales.action_write_product_to_nfc";
        public static final String ACTION_ORDER_PAYMENT_SUCCESS = "com.xiaomi.xms.sales.action_order_payment_success";
        public static final String ACTION_ORDER_REFUND = "com.xiaomi.xms.sales.action_order_refund";
        public static final String ACTION_SHOW_PRODUCT_DETAILS = "com.xiaomi.xms.sales.action_show_product_details";
        public static final String ACTION_SHOW_COMBO = "com.xiaomi.xms.sales.action_show_combo";
        public static final String ACTION_FETCH_VCODE = "com.xiaomi.xms.sales.action_fetch_vcode";
        public static final String ACTION_VERIFY_VCODE = "com.xiaomi.xms.sales.action_verify_vcode";
        public static final String ACTION_VERIFY_FCODE = "com.xiaomi.xms.sales.action_verify_fcode";
        public static final String ACTION_ADAPT_PHONE_LIST = "com.xiaomi.xms.sales.action_adapt_phone_list";
        public static final String ACTION_SHAKE = "com.xiaomi.xms.sales.action_shake";
        public static final String ACTION_FETCH_DEFENSE_HACKER_VCODE = "com.xiaomi.xms.sales.action_fetch_defense_hacker_vcode";
        public static final String ACTION_MIHOME_SCANNER = "com.xiaomi.xms.sales.action_mihome_scanner";
        public static final String ACTION_PRODUCT_SCANNER = "com.xiaomi.xms.sales.action_product_scanner";
        public static final String ACTION_PRODUCT_SCAN = "com.xiaomi.xms.sales.action_product_scan";
        public static final String ACTION_XIANHUO_SCAN = "com.xiaomi.xms.sales.action_xianhuo_scan";  //现货销售扫描SN
        public static final String ACTION_MIHOME_CHECK = "com.xiaomi.xms.sales.action_mihome_check";
        public static final String ACTION_MIHOME_PRODUCT_DETAIL = "com.xiaomi.xms.sales.action_mihome_product_detail";
        public static final String ACTION_PAYMENT_DIRECT = "com.xiaomi.xms.sales.action_direct";
        public static final String ACTION_SAMEDAYRETURN_SCAN = "com.xiaomi.xms.sales.action_same_day_return_scan";
		public static final String EXTRA_RETURN_ORDER_SN_STR = "com.xiaomi.xms.sales.action_return_order_sn_str";
		public static final String EXTRA_RETURN_SWITCH_TO_RETURN = "com.xiaomi.xms.sales.action_return_switch_to_return";
		public static final String EXTRA_RETURN_ORDER_STATUS_TYPE = "com.xiaomi.xms.sales.action_return_order_status_type";
		
		public static final String ACTION_SAMEDAYEXCHANGE_SCAN = "com.xiaomi.xms.sales.action_same_day_exchange_scan";
		public static final String EXTRA_EXCHANGE_ORDER_SN_STR = "com.xiaomi.xms.sales.action_exchange_order_sn_str";
		public static final String ACTION_SAMEDAYEXCHANGE_REPLACE_SN_SCAN = "com.xiaomi.xms.sales.action_exchange_replace_sn_scan";
		public static final String EXTRA_EXCHANGE_SRC_SN_STR = "com.xiaomi.xms.sales.action_exchange_src_sn_str";
		public static final String EXTRA_EXCHANGE_DEST_SN_STR = "com.xiaomi.xms.sales.action_exchange_dest_sn_str";
		
		public static final String ACTION_ATTENTION_NUMBER_TEST_ACTION = "com.xiaomi.xms.sales.action_attention_number_test_action";  
		public static final String ACTION_ATTENTION_WORD_TEST_ACTION = "com.xiaomi.xms.sales.action_attention_word_test_action";  
		public static final String ACTION_ATTENTION_PICTURE_TEST_ACTION = "com.xiaomi.xms.sales.action_attention_picture_test_action";  
		
		
		public static final String ACTION_PATIENT_SEARCH_SCAN = "com.xiaomi.xms.sales.action_patient_search_scan";
		public static final String EXTRA_PATIENT_SN_STR = "com.xiaomi.xms.sales.action_patient_sn_str";
    }

    public static final class Prefence {
        // 是否已经上传用户信息
        public static final String UPDATE_USER_INFO_TIME = "pref_update_user_info_time";
        // 程序是否第一次启动
        public static final String REF_ISFIRSTRUN = "pref_isfirstrun";
        // MIUI 是否对账户已经授权
        public static final String PREF_MIUI_ACCOUNT_AVAILABLE = "pref_miui_account_available";
        // 标识摇一摇中，某天已经不能再摇了
        public static final String PREF_NO_CHANCE = "pref_no_chance";
        // 上一次加载活动的Url地址
        public static final String PREF_ACTIVITY_URL = "pref_activity_url";
        // 活动版本，用来标识活动的唯一性
        public static final String PREF_ACTIVITY_VERSION = "pref_activity_version";
        // 设置中的Preference
        public static final String PREF_KEY_ENABLE_PUSH = "pref_key_enable_push";
        // 暂存的push id
        public static final String PREF_KEY_SHOP_PUSH_ID = "pref_key_shop_push_id";
        // splash信息
        public static final String PREF_KEY_SPLASH_INFO = "pref_key_splash_info";
        // 当前系统时间
        public static final String PREF_KEY_CURRENT_TIME = "pref_key_Current_time";
        public static final String PREF_KEY_MESSAGE_OVER_TIME = "pref_key_message_over_time";
        public static final String PREF_KEY_SMS_WG = "pref_key_sms_wg";
        public static final String PREF_PAYMENT_POS_SIGNIN_TIME = "pref_payment_pos_signin_time";
        public static final String PREF_CONNECTED_BLUETOOTH_DEVICE = "pref_connected_bluetooth_device";
        public static final String PREF_LOG_FILE_PATH = "pref_log_file_path";
        public static final String PREF_PREVIOUS_PAID_ORDER_ID = "pref_previous_paid_order_id";
    }

    public static final class SameDayReturn{
    	public static final String PREF_KEY_MERCHANT_NAME = "pref_return_key_merchant_name";
    	public static final String PREF_KEY_RETURN_ORDER_ID = "pref_return_key_order_id";
    	public static final String PREF_KEY_ORDER_DATE = "pref_return_key_order_date";
    	public static final String PREF_KEY_ORDER_DEVICE_NAME = "pref_return_key_order_device_name";
    	public static final String PREF_KEY_ORDER_FEE = "pref_return_key_order_fee";
    	public static final String PREF_KEY_CHOICE_RESULT = "pref_return_key_choice_result";
    	public static final String PREF_KEY_REFERENCE_NUMBER = "pref_return_key_reference_number";
    	public static final String PREF_KEY_POSREQUSTID = "pref_return_key_posrequstid";
    	public static final String PREF_KEY_SERVICENUMBER = "pref_return_key_servicenumber";
    }
    
    public static final class Account {
        // Sefault service id
        public static String DEFAULT_SERVICE_ID = "eshopmobile";

        // type
        public static final String ACCOUNT_TYPE = "com.xiaomi"; // ExtraIntent.XIAOMI_ACCOUNT_TYPE;
        public static final String UNACTIVATED_ACCOUNT_TYPE = "com.xiaomi.unactivated"; // ExtraIntent.XIAOMI_ACCOUNT_TYPE_UNACTIVATED;

        // pref
        public static final String PREF_SYSTEM_ASKED = "pref_asked_system";
        public static final String PREF_UID = "pref_uid";
        public static final String PREF_EXTENDED_TOKEN = "pref_extended_token";
        public static final String PREF_PASS_TOKEN = "pref_pass_token";
        public static final String PREF_LOGIN_SYSTEM = "pref_login_system";
        public static final String PREF_SYSTEM_UID = "pref_system_uid";
        public static final String PREF_SYSTEM_EXTENDED_TOKEN = "pref_system_extended_token";
        public static final String PREF_USER_NAMES = "pref_user_names";
        public static final String PREF_USER_ORGID = "pref_user_orgid";
        public static final String PREF_USER_ORGNAME = "pref_user_orgName";
        public static final String PREF_USER_AUTHS = "pref_user_auths";
        public static final String PREF_USER_NAME = "pref_user_name";
        public static final String PREF_NOTIFY_SERVER_ERROR_ORDERIDS = "pref_notify_server_error_orderids";
        public static final String PREF_POS_MAC_ADDRESS = "pref_pos_mac_address";

        // 用户名分隔符
        public static final String USER_NAME_SEPARATOR = ",";

        // URL
        public static final String ACCOUNT_URL_BASE = "https://account.xiaomi.com/pass"; // CloudManager.URL_ACCOUNT_BASE;
        public static final String API_URL_BASE = "http://api.account.xiaomi.com/pass"; // CloudManager.URL_ACOUNT_API_BASE;

        public static final String URL_LOGIN_AUTH = ACCOUNT_URL_BASE + "/serviceLoginAuth";
        public static final String URL_LOGIN = ACCOUNT_URL_BASE + "/serviceLogin";
        public static final String URL_QUERY_PHONE = API_URL_BASE + "/activate/dev/%s/activating";
        public static final String URL_REG = API_URL_BASE + "/user/full";
        public static final String URL_USER_EXISTS = API_URL_BASE + "/user@id";
        public static final String URL_RESEND_EMAIL = API_URL_BASE + "/sendActivateMessage";
        public static final String URL_PASSWORD_RECOVERY = ACCOUNT_URL_BASE + "/forgetPassword";
        // public static final String URL_QUERY_SMS_GW = API_URL_BASE +
        // "/configuration";
        public static final String URL_QUERY_SMS_GW = HostManager.URL_XIAOMI_SHOPAPI_ROOT + "/pass/config";

        // SMS gateway for China Mobile
        public static final String SMS_GW_CM = "106571014010030";
        // SMS gateway for China Unicom
        public static final String SMS_GW_CU = "1065507729555678";
        // SMS gateway for China Telecom
        public static final String SMS_GW_CT = "10659057100335678";
        // SMS gateway for default
        public static final String SMS_GW_DEFAULT = "+447786209730";

        // extra
        public static final String EXTRA_SERVICE_URL = "extra_service_url";
        public static final String EXTRA_USRERID = "extra_username";
        public static final String EXTRA_ACCOUNT = "extra_account";
        public static final String EXTRA_RESET_COUNT = "extra_reset_count";
        public static final String EXTRA_UPDATE_TYPE = "extra_update_type";

        // action
        public static final String ACTION_ACCOUNT_SETTINGS = "android.settings.XIAOMI_ACCOUNT_SYNC_SETTINGS";

        // 帐号发生变化时发送的intent
        public static final String ACTION_LOGIN_ACCOUNTS_PRE_CHANGED = "android.accounts.LOGIN_ACCOUNTS_PRE_CHANGED";

        public static final int TYPE_REMOVE = 1;
        public static final int TYPE_ADD = 2;

        // reg type
        public static final String REG_TYPE_PHONE_NUMBER = "reg_sms";
        public static final String REG_TYPE_EMAIL = "reg_email";

        /**
         * 1=reg via email, 2=reg via sms, otherwise indicates the account has
         * been activated
         */
        public static final String ACCOUNT_REG_TYPE = "reg_type";
        public static final String ACCOUNT_REG_EMAIL = "reg_email";
        public static final String ACCOUNT_REG_PHONE = "reg_phone";
        
       
        
    }

    public static final class RequestCode {
        public static final int CODE_ADDRESS = 1;
        public static final int CODE_REQUEST_HOME_FULL_SCREEN = 2;
        public static final int CODE_REQUEST_SIGIN = 3;
        public static final int CODE_REQUEST_SIGUP = 4;
        public static final int CODE_REQUEST_EDIT_ORDER = 5;
    }

    public static final class AddShoppingCartStatus {
        public static final String ADD_SUCCESS = "add_success";
        public static final String ADD_FAIL = "add_fail";
        public static final String ADD_FAIL_ALREADY_MAX = "add_fail_already_max";
    }
    
    public static final class IcuGradeResult {
        public static final String RASS_RESULT = "rass_result";
        public static final String ATTENTION_RESULT = "attention_result";
        public static final String THINK_RESULT = "think_result";
        public static final String RASS_RESULT_BOOLEAN = "rass_result_boolean";
        public static final String THINK_RESULT_BOOLEAN = "think_result_boolean";
        public static final String ATTENTION_RESULT_BOOLEAN = "attention_result_boolean";
    }

    // 解析移动版的m.xiaomi.com的URI
    public static final class MobileWebUri {
        public static final String QUERY_SEPARATOR = "?";
        public static final String FRAGMENT_SEPARATOR = "#";
        public static final String QUERY_PARAM_ACTION = "ac";
        public static final String QUERY_PARAM_ACTION_PRODUCT = "product";
        public static final String QUERY_PARAM_OPTION = "op";
        public static final String QUERY_PARAM_OPTION_LIST = "list";
        public static final String QUERY_PARAM_OPTION_VIEW = "view";
        public static final String QUERY_PARAM_OPTION_LIST_ID = "cate_id";
        public static final String QUERY_PARAM_OPTION_VIEW_ID = "product_id";
    }

    public static final class ExternalStorage {
        public static final String ROOT = Environment.getExternalStorageDirectory() + "/xmssales";
        public static final String ROOT_SAVED = ROOT + "/save/";
    }

    public static final class Schema {
        public static final String CALL_PHONE_SCHEMA = "tel:";
    }

    public static final class OrderExpressType {
        public static final String ORDER_EXPRESS_LIST_TYPE_HEAD = "express_list_type_head";
        public static final String ORDER_EXPRESS_LIST_TYPE_DEFAULT = "express_list_type_default";
    }

    public static final class Bluetooth {
        public static final int STATE_OFF = 10;
        public static final int STATE_TURNING_ON = 11;
        public static final int STATE_ON = 12;
        public static final int STATE_TURNING_OFF = 13;
    }
    
    public static Set<Integer> picSet = new HashSet<Integer>();
    
    public static int[] mFirstPictures = {R.drawable.table2,R.drawable.key2,R.drawable.hammer2,R.drawable.cup2,R.drawable.car2};
    
    public static int[] mSecondPictures = {R.drawable.table2,R.drawable.truck2,R.drawable.hammer2,R.drawable.chainsaw2,
    	R.drawable.lock2,R.drawable.cup2,R.drawable.car2,R.drawable.key2,R.drawable.glass2,R.drawable.chair2};
    public static int[] correctAnswer = {0,2,5,6,7};
    
    //0,2,5,6,7
}
