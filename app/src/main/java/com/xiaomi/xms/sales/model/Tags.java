
package com.xiaomi.xms.sales.model;

import android.text.TextUtils;

import org.json.JSONObject;

public class Tags {
    /** Common tags for each api **/
    public static final String RESULT = "result";
    public static final String DESCRIPTION = "description";
    public static final String REASON = "reason";
    public static final String DATA = "data";
    public static final String CODE = "code";
    public static final String STATUS = "status";
    public static final String HEADER = "header";
    public static final String BODY = "body";
    public static final String DESC = "desc";

    /** Common values for result **/
    public static final String RESULT_OK = "ok";
    public static final String RESULT_TRUE = "true";
    public static final String RESULT_CODE = "200";
    public static final String RESULT_ERROR = "error";

    public static boolean isJSONResultOK(JSONObject json) {
        return json != null && RESULT_OK.equals(json.optString(RESULT));
    }

    public static boolean isJSONReturnedOK(JSONObject json) {
        return json != null && TextUtils.equals(RESULT_CODE, json.optJSONObject(HEADER).optString(CODE));
    }

    /** Category tags **/
    public interface Category {
        public static final String CATEGORIES = "categroies";// 分类
        public static final String CATE_ID = "cate_id"; // 分类ID
        public static final String CATE_NAME = "cate_name"; // 分类名称
        public static final String TOTAL_COUNT = "total_count"; // 分类下商品数量
        public static final String IMAGE_URLS = "image_url"; // 图片地址
    }

    public static final class CategoryTree {
        public static final String CHILDREN = "children";// 分类
        public static final String CAT_ID = "cat_id"; // 分类ID
        public static final String CAT_NAME = "cat_name"; // 分类名称
        public static final String HAS_CHILDREN = "has_children"; // 是否有子分类
        public static final String IMAGE_URLS = "image_url"; // 图片地址
        public static final String DATA_TYPE = "data_type";
    }

    /** Product tags **/
    public interface Product {
        public static final String TOTAL_COUNT = "total_count"; // 分类下商品数量
        public static final String TOTAL_PAGES = "total_pages"; // 总页数
        public static final String CURRENT_PAGE = "current_page"; // 当前页数
        public static final String CATE_NAME = "cate_name"; // 分类名
        public static final String PRODUCT = "product";
        public static final String PRODUCT_ID = "product_id"; // 商品ID
        public static final String PRODUCT_NAME = "product_name"; // 商品名称
        public static final String PRODUCT_COUNT = "product_count"; // 商品数量
        public static final String PRICE = "price"; // 小米商城价格
        public static final String MARKET_PRICE = "market_price"; // 市场价格，当为促销价时，这个价可显示
        public static final String STYLE_NAME = "style_name"; // 当前商品的风格样式
        public static final String IMAGE_URL = "image_url"; // 商品图片
        public static final String URL = "url"; // 商品图片
        public static final String IS_COS = "is_cos"; // 是否缺货 TRUE(缺货)
                                                      // FALSE(不缺货)
        public static final String P_ID = "p_id";// 评论用的id
        public static final String IS_BATCHED = "is_batched";// 是否是套餐
        public static final String CONTAINID = "containid";// 套餐ID

        public static final String DISPLAY_TYPE = "display_type"; // 显示方式

        public static final String DISPLAY_NATIVE = "display_native";// Android本地显示
        public static final String DISPLAY_WEB = "display_web"; // Android
                                                                // webview显示
        public static final String DISPLAY_BROWSER = "display_browser"; // 浏览器显示
    }

    /** miphone tags **/
    public static final class MiPhone {
        public static final String PRODUCT_ID = "phone_type"; // 商品id
        public static final String PRODUCT_NAME = "phone_name"; // 商品名称
        public static final String PRICE = "price"; // 小米商城价格
        public static final String BRIEF = "brief"; // 商品图片
        public static final String DESCRIBE = "describe";
        public static final String DESCRIBE_TWO = "describe_two";
        public static final String LEFT_BUTTON = "left_button";
        public static final String LEFT_URL = "left_url";
        public static final String RIGHT_BUTTON = "right_button";
        public static final String RIGHT_URL = "right_url";
        public static final String PRODUCT_URL = "product_url";
        public static final String IMAGE_URL = "image_url"; // 商品图片
        public static final String DISPLAY_TYPE = "display_type"; // 显示方式
    }

    /** Category tags **/
    public static final class ShoppingCartList {
        public static final String PRODUCT_NAME = "product_name"; // 商品名称
        public static final String BUY_LIMIT = "buy_limit"; // 最多能购买数量
        public static final String NUM = "num"; // 商品数量
        public static final String SALE_PRICE = "salePrice"; // 商品价格
        public static final String SUB_TOTAL = "subtotal"; // 商品总额
        public static final String ITEM_ID = "itemId"; // 购物车子项id
        public static final String IMAGE_URL = "image_url"; // 图片地址
        public static final String IMAGE_PHOTO = "800"; // 图片尺寸
        public static final String IMAGE_THUMBNAIL = "180";
        public static final String TOTAL_PRICE = "totalprice";
        public static final String COUNT = "count";
        public static final String TOTAL = "total";
        public static final String ITEMS = "items";
        public static final String CAN_CHANGE_NUM = "can_change_num";
        public static final String CAN_DELETE = "can_delete";
        public static final String SHOWTYPE = "showType";
        public static final String SHOWTYPE_BARGIN = "bargain";
        public static final String SHOWTYPE_GIFT = "gift";
        public static final String SHOWTYPE_SECKILL = "seckill";
        public static final String SHOWTYPE_SPECIAL = "special";
        public static final String SHOWTYPE_ERNIE = "ernie";
        public static final String ADAPT = "adapt"; // 适配机型
        public static final String GATHER_ORDER_INFO = "gatherorder_info";// 凑单信息
        public static final String SHOW_LIST = "show_list";// 是否可以凑单
        public static final String BALANCE_PRICE = "balance_price";// 凑单差额
        public static final String GOOD_LIST = "goods_list";// 凑单商品列表
        public static final String ACTIVITYS = "activitys";// 活动列表
        public static final String ACTNAME = "actName";// 活动信息
        public static final String REDUCTION = "reduction";// 满减活动
        public static final String POSTFREE = "postFree";// 免邮活动
        public static final String GIFT = "gift";// 赠品活动
        public static final String COUPONS = "coupons";// 赠券活动
        public static final String PROPERTIES = "properties";
    }

    public static final class ShoppingSupply {
        public static final String BARGAINS = "bargains"; // 加价购的上层node
        public static final String ACT_ID = "actId"; // 活动id
        public static final String BARGAIN_NAME = "bargain_name"; // 活动名称
        public static final String PRODUCT_ID = "product_id"; // 商品id
        public static final String CHECKED = "checked"; // 是否选中
        public static final String SELECTABLE = "selectable"; // 是否可选
        public static final String SELECT_INFO = "selecInfo"; // 可选商品信息
        public static final String ITEM_ID = "item_Id";
        public static final String SELECTABLE_PRODUCTS = "selectable_products";
        public static final String BOUGHT_PRODUCT_ID = "bought_product_id";
    }

    public static final class OrderSubmit {
        public static final String TOTAL = "total"; // 购物车中商品总数
        public static final String AMOUNT = "amount"; // 用户需支付的总额（含运费）
        public static final String SHIPMENT = "shipment"; // 运费金额
        public static final String CARTLIST = "cartlist"; // 商品列表
        public static final String PRODUCT_MONEY = "productMoney"; // 商品总价（未抵扣之前的原始价格）
        public static final String ACTIVITY_DISCOUNT_MONEY = "activityDiscountMoney"; // 活动优惠金额（例如参加满100减10等活动优惠的金额）
        public static final String COUPON_DISCOUNT_MONEY = "couponDiscountMoney"; // 优惠券优惠的金额
        public static final String AMOUNT_DESC = "amountDesc"; // 价格计算公式

        public static final String ADDRESS_ID = "address_id";
        public static final String PAY_ID = "pay_id";
        public static final String PICKUP_ID = "pickup_id";
        public static final String SHIPMENT_ID = "shipment_id";
        public static final String BEST_TIME = "best_time";
        public static final String INVOICE_TYPE = "invoice_type";
        public static final String INVOICE_TITLE = "invoice_title";
        public static final String COUPON_TYPE = "coupon_type";
        public static final String COUPON_CODE = "coupon_code";
        public static final String CHECK_CODE = "check_code";
        public static final String NEEDCHECKCODE = "needcheckcode";
        public static final String MIHOME_BUY_ID = "client_mihome_id";
        public static final String EXTEND_FIELD = "extend_field";
        public static final String EXTEND_FIELD_CONSIGNESS = "consignee";
        public static final String EXTEND_FIELD_TEL = "tel";
        public static final String SERVICENUMBER = "serviceNumber";
        public static final String TOTALPRICE = "totalPrice";
        public static final String ADDDATE = "addDate";
    }

    public static final class DelCart {
        public static final String ITEM_ID = "itemId";
        public static final String ITEM_IDS = "itemIds";
        public static final String MIHOME_BUY_ID = OrderSubmit.MIHOME_BUY_ID;
    }

    public static final class EditConsumption {
        public static final String ITEM_ID = "itemId";
        public static final String ITEM_IDS = "itemIds";
        public static final String CONSUMPTION = "consumption";
        public static final String MIHOME_BUY_ID = OrderSubmit.MIHOME_BUY_ID;
    }

    public static final class CheckoutSubmit {
        public static final String CHECKED = "checked";
        public static final String PAYLIST = "paylist";
        public static final String SHIPMENTLIST = "shipmentlist";
        public static final String DELIVERTIME = "delivertime";
        public static final String INVOICE = "invoice";
        public static final String DESC = "desc";
        public static final String VALUE = "value";
        public static final String BRIEF = "brief";
        public static final String PAY_ID = "pay_id";
        public static final String SHIPMENT_ID = "shipment_id";
        public static final String ADDRESS_ID = "address_id";
        public static final String LIST = "list";
        public static final String HOME_ID = "home_id";
        public static final String NAME = "name";
        public static final String ADDRESS = "address";
        public static final String PICKUP_ID_SELF = "6";
        public static final String PICKUP_ID_DEFAULT = "0";
        public static final String INVOICE_ID_COMPANY = "2";
        public static final String INVOICE_ID_PERSONAL = "1";
        public static final String INVOICE_OPEN = "invoice_open";
        public static final String MIHOME_BUY_ID = OrderSubmit.MIHOME_BUY_ID;
        public static final String rass_test_1 = "4";
		public static final String rass_test_2 = "3";
		public static final String rass_test_3 = "2";
		public static final String rass_test_4 = "1";
		public static final String rass_test_5 = "0";
		public static final String rass_test_6 = "-1";
		public static final String rass_test_7 = "-2";
		public static final String rass_test_8 = "-3";
		public static final String rass_test_9 = "-4";
		public static final String rass_test_10 = "-5";
		public static final String rass_test_11 = "11";
    }

    /** UserInfo tags **/
    public static final class UserInfo {
        public static final String JSON_KEY_USER_ID = "miliao";
        public static final String JSON_KEY_USER_NAME = "username";
        public static final String JSON_KEY_NAME = "name";
        public static final String JSON_KEY_ORGID = "orgId";
        public static final String JSON_KEY_ORGNAME = "miHomeName";
        public static final String JSON_KEY_AUTHS = "auths";
    }

    public static final class RemindInfo {
        public static final String JSON_KEY_TOPAYCOUNT = "toPayCount";
        public static final String JSON_KEY_TOARRIVECOUNT = "toArriveCount";
        public static final String JSON_KEY_REVIEW = "wait_comment";
    }

    // Home tags
    public static final class Home {
        public static final String ITEM_COUNT = "item_count";
        public static final String ITEMS = "items";
        public static final String ITEM_TYPE = "item_type";
        public static final String THUMBNAIL_URL = "image_url";
        public static final String PHOTO_URL = "big_image_url";
        public static final String ACTIVITY_URL = "activity_url";
        public static final String PRODUCT = "product";
        public static final String PRODUCT_ID = "product_id";
        public static final String PRODUCT_NAME = "product_name";
        public static final String PRODUCT_DETAIL = "product_detail";
        public static final String PRODUCT_PRICE = "product_price";
        public static final String FULL_PRICE = "full_price";
        public static final String ACTIVITY_ICON = "activity_icon";
        public static final String BIG_PHOTO_URL = "big_image";

    }

    /** ProductDetails tags **/
    public static final class ProductDetails {
        public static final String PRODUCT_NAME = "product_name"; // 商品名称
        public static final String PRODUCT_ID = "product_id"; // 商品ID
        public static final String PRICE = "price"; // 小米商城价格
        public static final String RESULT = "result";
        public static final String PRODUCT_BRIEF = "product_brief"; // 商品简介
        public static final String ADAPT = "adapt"; // 适配手机
        public static final String ELEMENTS_PRODUCT = "elements_goods"; // 如是是套餐，这里显示套餐组合列表
        public static final String ATTRS = "attrs"; // 商品属性
        public static final String BUY_LIMIT = "buy_limit"; // 最多能购买数量
        public static final String IMAGE_URL = "image_url"; // 图片地址
        public static final String IMAGES = "images"; // 产品的相册列表
        public static final String IS_COS = "is_cos"; // 是否缺货 TRUE(缺货)
                                                      // FALSE(不缺货)
        public static final String IS_CHANGE_STYLE = "is_change_style"; // 是否需要选择套餐组合
                                                                        // TRUE(需要)
                                                                        // FALSE(不需要)
        public static final String PRODUCT_DESC_IMG = "product_desc_img";
        public static final String PRODUCT_STYLE = "style";// 同类产品下的不同样式列表
        public static final String STYLE_NAME = "style_name"; // 当前商品的风格样式
        public static final String MARKET_PRICE = "market_price"; // 市场价格，当为促销价时，这个价可显示
        public static final String IMAGES_ONE = "1";// 产品的相册第一张
        public static final String LAST_ITEM = "last_item"; // 前一个商品ID
        public static final String NEXT_ITEM = "next_item"; // 下一个商品ID
        public static final String IS_PHONE = "is_phone"; // 是否是手机详情页
        public static final String CANJOINACTS = "canJoinActs"; // 商品参与的活动
        public static final String TYPE_DESC = "typeDesc"; // 商品参与的活动的类型
        public static final String TITLE = "title"; // 商品参与的活动的类型描述
        public static final String PRODUCT_NEW_STYLE = "new_style";// 同类产品下的不同样式列表
        public static final String PRODUCT_STYLE_TYPE = "name";
        public static final String PRODUCT_STYLE_DATA = "data";
        public static final String PRODUCT_STYLE_ID = "key";
        public static final String PRODUCT_STYLE_NAME = "value";
    }

    /** Address tags **/
    public static final class AddressInfo {
        public static final String ID = "address_id"; // 地址id
        public static final String CONSIGNEE = "consignee"; // 收件人
        public static final String COUNTRY = "country"; // 国家
        public static final String PROVINCE = "province"; // 省份
        public static final String CITY = "city"; // 城市
        public static final String DISTRICT = "district"; // 地区
        public static final String ADDRESS = "address"; // 地址
        public static final String ZIPCODE = "zipcode"; // 邮编
        public static final String TEL = "tel"; // 电话
        public static final String AREA_ID = "id"; // id
        public static final String AREA_NAME = "name"; // name
        public static final String CHINA_ID = "1"; // area id of china
        public static final String MIHOME_BUY_ADDR = "mihome_buy_addr";

        public static final int ERROR_CODE_CONSIGNEE = 2013001;
        public static final int ERROR_CODE_PROVINCE = 2013003;
        public static final int ERROR_CODE_CITY = 2013004;
        public static final int ERROR_CODE_DISTRICT = 2013005;
        public static final int ERROR_CODE_LOCATION = 2013006;
        public static final int ERROR_CODE_ZIPCODE = 2013007;
        public static final int ERROR_CODE_TEL = 2013008;
    }

    /** mihome buy info tags **/
    public static final class MihomeBuyInfo {
        public static final String SELF = "mihome";
        public static final String HOME_ID = "home_id";
        public static final String PROVINCE = "province";
        public static final String CITY = "city";
        public static final String DISTRICT = "district";
        public static final String NAME = "name";
        public static final String ADDRESS = "address";
        public static final String TEL = "tel";
        public static final String STATUS = "status";
    }

    /** Order info tags **/
    public static final class Order {
        public static final String ID = "order_id"; // 订单号
        public static final String STATUS = "order_status"; // 支付状态
        public static final String CONSIGNEE = "consignee"; // 收货人
        public static final String ADDRESS = "address"; // 收货人地址
        public static final String INVOICE_TITLE = "invoiceTitle";// 发票信息
        public static final String BEST_TIME = "best_time";// 收货时间
        public static final String FEE = "goods_amount";// 货物总金额
        public static final String CONSIGNEE_PHONE = "tel";// 收货人电话号码
        public static final String TYPE = "type"; // 订单类型
        public static final String ADD_TIME = "add_time"; // 订单添加时间
        public static final String STATUS_INFO = "order_status_info"; // 订单状态信息obj
        public static final String ORDER_STATUS_DESC = "orderStatusDesc"; // 订单状态信息obj
        public static final String DEVICE_NAME = "deviceName";
        public static final String STATUS_NEXT = "next";
        public static final String STATUS_SHOWTYPE = "showtype";
        public static final String STATUS_STRING = "info"; // 订单状态
        public static final String PRODUCT = "product"; // 商品列表
        public static final String IMAGE_URL = "image_url"; // 图片列表
        public static final String IMAGE_SIZE = "180"; // 图片尺寸
        public static final String TRACK_INFO = "order_track_info"; // 流程信息
        public static final String PROVINCE = "province"; // 省
        public static final String CITY = "city"; // 市
        public static final String DISTRICT = "district"; // 区
        public static final String AREA_NAME = "name"; // 地区名
        public static final String AREA_ID = "id";
        public static final String ZIPCODE = "zipcode"; // 邮编
        public static final String PRODUCT_ID = "product_id"; // 产品id
        public static final String PRODUCT_NAME = "product_name"; // 产品名称
        public static final String PRODUCT_COUNT = "product_count"; // 产品数量
        public static final String CART_PRICT = "cart_price"; // 产品价格
        public static final String TOTAL_PRICT = "subtotal"; // 产品价格
        public static final String TRACK_TEXT = "text"; // 步骤
        public static final String TRACK_TIME = "time"; // 时间
        public static final String HAS_PHONE = "hasPhone"; // 是否包含手机
        public static final String MIHOME_BUY_ID = OrderSubmit.MIHOME_BUY_ID;
        public static final String PICKUP_INFO = "pickup_info";
        public static final String PICKUP_ADDRESS = "address";
        public static final String PICKUP_NAME = "name";
        public static final String PICKUP_TEL = "tel";
        public static final String PICKUP_LONLAT = "longitude_latitude";
        public static final String RETURN_POSREQUSTID = "PosRequstId";
		public static final String RETURN_REFERENCENUMBER = "ReferenceNumber";
		public static final String RETURN_PAY_STATUS = "Pay_Status";
        // 物流
        public static final String EXPRESS = "express";
        public static final String EXPRESS_ID = "express_id";
        public static final String EXPRESS_SN = "express_sn";
        public static final String EXPRESS_UPDATE_TIME = "express_update_time";
        public static final String EXPRESS_NAME = "express_name";
        public static final String EXPRESS_TRACE = "express_trace";
        public static final String EXPRESS_SHOW = "is_show";
        public static final String EXPRESS_TRACE_TEXT = "track";
        public static final String EXPRESS_TRACE_TIME = "time";

        public static final int PAYMENT_STATUS_WAIT_PAY = 3; // 等待付款
        public static final int PAYMENT_STATUS_OK = 4; // 已经支付
        public static final int PAYMENT_STATUS_WAIT_NOTIFY_WAREHOUSE = 49; // 待通知仓库发货
        public static final int PAYMENT_STATUS_NOTIFIED_WAREHOUST = 50; // 已通知仓库发货
        public static final int PAYMENT_STATUS_CONTRACTPHONE_WAIT_VERIFY = 52; // 合约机待审核
        public static final int PAYMENG_STATUS_SHIPPED = 7; // 已经发货
        public static final int PAYMENG_STATUS_RECEIPT = 8; // 已收货
        // 订单类型
        public static final String ORDER_STATUS_OPEN = "0"; // 订单列表
        public static final String ORDER_STATUS_CLOSE = "18"; // 已关闭订单
        public static final String ORDER_STATUS_CHANGE = "3"; // 换货单
        public static final String ORDER_STATUS_REFUND = "6"; // 退款单
        public static final String ORDER_STATUS_WAIT_PAYMENT = "1"; // 待付款订单
        public static final String ORDER_STATUS_EXPRESS = "4"; // 已付款但未收货的订单
        public static final String ORDER_STATUS_CAN_EDIT = "1,4,7"; // 可修改订单

        public static final String ORDER_NEXT_PAY = "PAY_MONEY";
        public static final String ORDER_NEXT_CONFIRM = "CONFIRM_ORDER";
        public static final String ORDER_NEXT_CANCEL = "CANCLE_ORDER";
        // 订单详情V2
        public static final String DELIVERS = "delivers";
        public static final String DELIVER_ID = "deliver_id";
        public static final String SHIPMENT_EXPRENSE = "shipment_expense";
        public static final String ORDER_STATUS_INFO = "order_status_info";
        public static final String ORDER_TRACE = "trace";
        public static final String REDUCE_PRICE = "reduce_price";// 优惠的价格
        public static final String ORIGINAL_PRICE = "original_price";// 订单原价(优惠之前的价格)
        // 修改订单信息是否需要短信验证
        public static final String IS_MESSAGE_CHECK = "is_message_check";
    }

    /** ComboList tags **/
    public static final class ComboList {
        public static final String RESULT = "result";
        public static final String PRODUCT_ID = "product_id"; // 商品ID
        public static final String PRODUCT_NAME = "product_name"; // 商品名称
        public static final String PRICE = "price"; // 小米商城价格
        public static final String PRODUCT_STYLE = "style_name"; // 当前商品的风格样式
        public static final String IMAGE_URL = "image_url"; // 图片地址
        public static final String IS_SALE = "is_cos"; // 是否缺货 TRUE(缺货)
                                                        // FALSE(不缺货)
    }

    /** Order info tags **/
    public static final class Lottery {
        public static final String ITEMS = "items";
        public static final String ITEM_TYPE = "item_type";
        public static final String IMAGE_URL = "image_url";
        public static final String URL = "url";

        public static final String NATIVE_TYPE_SHAKE = "shake";
        public static final String NATIVE_TYPE_COCACOLA = "cocacola";
        public static final String NATIVE_TYPE_SCANNERBUY = "scanner";
        public static final String NATIVE_TYPE_RECHARGE = "recharge";
        public static final String KEY_NATIVE_TYPE_LIST = "nativelist";
    }

    public static final class Phone {
        public static final String M1_PHONE = "1";// 小米手机1
        public static final String M11S_PHONE = "2";// 小米手机1s
        public static final String M2_PHONE = "4";// 小米手机2
        public static final String MI_BOX = "8";// 小米盒子
        public static final String M22S_PHONE = "16";// 小米手机2s
        public static final String M2A_PHONE = "32";// 小米手机2a
        public static final String MRED_PHONE = "64";// 红米
        public static final String M3_PHONE = "128";// 米3
        public static final String MI_TV = "256";// 米TV
        public static final String ALL_PHONE = "0";
        public static final String ALL_PHONETYPE = "-1";
    }

    public static final class Activity {
        public static final String STATUS = "status"; // 是否有活动
        public static final String URL = "url"; // 活动链接
        public static final String VERSION = "version"; // 活动版本
        public static final String TYPE = "type";// 活动类型

        public static final String TYPE_RESERVE = "reserve"; // 预约
        public static final String TYPE_BUY = "buy"; // 抢购
    }

    public static final class VersionUpdate {
        public static final String NEED_UPDATE = "needUpdate";
        public static final String UPDATE_URL = "url";
        public static final String UPDATE_VERSION = "apkVersion";
        public static final String VERSIONNAME = "versionName";
        public static final String UPDATEINFO = "updateInfo";
    }

    public static final class CheckCode {
        public static final String URL = "url";
        public static final String COOKIE_AUTHCODE = "authcode";
        public static final String GOODS = "targetGoods";
        public static final String LIST = "product_list";
    }

    /** MiPhoneDetails tags **/
    public static final class MiPhoneDetails {
        public static final String FOCUS_IMG = "focus_img";
        public static final String PHONE_TYPE = "phone_type";
        public static final String FEATURES = "features";
        public static final String FEATURE_NAME = "feature_name";
        public static final String DETAILS = "details";
        public static final String IMG = "img";

        public static final String MEDIA = "media";
        public static final String URL = "url";
        public static final String TEXT = "text";

        public static final String GALLERY = "gallery";
        public static final String PRODUCT_IMG = "product_img";
        public static final String PRODUCT_NAME = "product_name";
        public static final String PRODUCT_ID = "product_id";
        public static final String PRODUCT_PRICE = "product_price";
        public static final String PRODUCT_BRIEF = "product_brief";
        public static final String IS_AVAIL = "is_avail";
        public static final String LAST_ITEM = "last_item"; // 前一个商品ID
        public static final String NEXT_ITEM = "next_item"; // 下一个商品ID
        public static final String IS_PHONE = "is_phone"; // 是否是手机详情页
        public static final String ACTIVITY_URL = "activity_url"; // 手机详情页web链接
    }

    public static final class FCodeSelectProduct {
        public static final String PRODUCT_ID = "product_id";
        public static final String PRICE = "price";
        public static final String IMAGE_URL = "image_url";
        public static final String SIZE = "180";
        public static final String SIZE_SINGLE = "800";
        public static final String NAME = "product_name";
        public static final String BRIEF = "product_brief";
        public static final String IS_CHANGE_STYLE = "is_change_style";
    }

    public static final class PhoneModel {
        public static final String NAME = "name";
        public static final String SYMBOL = "symbol";
        public static final String CODE = "code";
        public static final String TEXT = "text";
        public static final String IMAGE_URL = "image_url";
    }

    public static final class SaleOutRegister {
        public static final String MORE = "more";
    }

    /** MiHome **/
    public static final class MiHome {
        public static final String TEL_SEPARATOR1 = "/";
        public static final String TEL_SEPARATOR2 = "、";
        public static final String TEL_SEPARATOR3 = " "; // 空格
        public static final String TEL_SEPARATOR4 = "\\"; // \ 前面加转义字符\
    }

    public static final class EditOrder {
        public static final String USER_ID = "user_id";
        public static final String ORDER_ID = "order_id";
        public static final String TYPE = "type";
        public static final String BEST_TIME = "best_time";
        public static final String CONSIGNEE = "consignee";
        public static final String ADDRESS = "address";
        public static final String ZIPCODE = "zipcode";
        public static final String TEL = "tel";
        public static final String COUNTRY = "country";
        public static final String PROVINCE = "province";
        public static final String CITY = "city";
        public static final String DISTRICT = "district";

        public static final String VALUE_TYPE_TIME = "time";
        public static final String VALUE_TYPE_ADDRESS = "address";

        public static final String CHECKCODE = "checkcode";
        public static final String ORDER_MODIFY = "order_modify";
    }

    public static final class MihomeCheckInfo {
        public static final String CLIENT_MIHOME_ID = "mihome_id";
        public static final String SIGNS = "signs";
        public static final String MIHOME_NAME = "mihome_name";
        public static final String IMAGE_URL = "image_url";
        public static final String DESC = "text";
        public static final String SIGNIN_COUNT = "signin_count";
        public static final String COLOR = "background_color";
        public static final String TDCODE = "tdcode";
    }

    /** Look up comments info for the specified product. */
    public final class CommentInfo {
        public static final String COMMENTS = "comments";
        public static final String USER_NAME = "user_name";
        public static final String COMMENT_CONTENT = "comment_content";
        public static final String ADD_TIME = "add_time";
        public static final String AVERAGE_GRADE = "average_grade";
        public static final String COMMENTS_GOOD = "comments_good";
        public static final String COMMENTS_GENERAL = "comments_general";
        public static final String COMMENTS_BAD = "comments_bad";
        public static final String TOTAL_COUNT = "total_count";
    }

    /** Help the user to write a review. */
    public final class Review {
        public static final String GOODS_ID = "goods_id";
        public static final String GOODS_NAME = "goods_name";
        public static final String GOODS_IMG = "goods_img";
        public static final String ARRIVE_TIME = "arrive_time";
        public static final String IS_COMMENT = "isComment";
        public static final String GOODS_LIST = "goodsList";
        public static final String SHOP_PRICE = "shop_price";
    }

    public final class MiHomeStorage {
        public static final String BODY = "body";
        public static final String RESULTS = "results";
        public static final String STORAGE_AMOUNT = "storage_amount";
    }

    public static final class RequestKey {
        public static final String HEADER = "header";
        public static final String APPID = "appid";
        public static final String KEY = "key";
        public static final String URL = "url";
        public static final String METHOD = "method";
        public static final String SIGN = "sign";
        public static final String BODY = "body";
        public static final String ORG_ID = "org_id";
        public static final String DATA = "data";
        public static final String OPERATORID = "operatorId";
        public static final String OPERATORMIHOME = "operatorMihome";
        public static final String APITYPE = "apitype";
    }

    public static final class RequestValue {
        public static final String APPID = "xm_1004";
        public static final String KEY = "1a3015121bb93b05de89128de42f98ca";
        public static final String APITYPE = "1";
    }

    public static final class XMSAPI {
        public static final String USERID = "userId";
        public static final String IMEI = "imei";
        public static final String ORGID = "orgId";
        public static final String KEYWORDS = "keywords";
        public static final String OPTYPE = "opType";
        public static final String REPLACESN = "replaceSn";
    }
}
