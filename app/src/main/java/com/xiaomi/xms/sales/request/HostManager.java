
package com.xiaomi.xms.sales.request;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Device;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class HostManager {
    private static final String TAG = "HostManager";

    public static final class Parameters {
        public static final class Keys {
            // 是否压缩结果
            public static final String COMPRESS = "compressed";
            public static final String COMPRESS_HEADER = "Compressed";
            // 机型
            public static final String PHONE_MODEL = "phone_model";
            public static final String PHONE_DEVICE = "phone_device";
            // APP Id
            public static final String CLIENT_ID = "client_id";
            // 目录 Id
            public static final String CATEGORY_ID = "cateid";
            // categoryTree rootid
            public static final String ROOT_ID = "root_id";
            // 分页页数
            public static final String PAGE_INDEX = "pageindex";
            // 每页大小
            public static final String PAGE_SIZE = "pagesize";
            // 商品列表
            public static final String PRODUCT = "product";
            // Search关键字
            public static final String KEYWORD = "keyWord";
            // 商品Id
            public static final String PRODUCT_ID = "product_id";
            // 套餐商品ids
            public static final String ITEM_IDS = "itemIds";
            // 来源
            public static final String SOURCE = "source";
            // 地址Id
            public static final String ADDRESS_ID = "address_id";
            // 手机分辨率
            public static final String DEVICE_DENSITY = "display_density";
            // 订单号
            public static final String ORDER_ID = "order_id";
            // 支付途径
            public static final String PAY_ONLINE_BANK = "payOnlineBank";
            // User id
            public static final String USER_ID = "userId";
            // Pass token
            public static final String PASS_TOKEN = "passToken";
            // Service token
            public static final String SERVICE_TOKEN = "serviceToken";
            // Cookies 中表示Android app的字段
            public static final String COOKIE_NAME_PLATFORM = "Android_native";
            // Security payment key
            public static final String SECURITY_PAYMENT_KEY = "security";
            public static final String ADDRESS_CONSIGNEE = "consignee";
            public static final String ADDRESS_PROVINCE = "province_id";
            public static final String ADDRESS_CITY = "city_id";
            public static final String ADDRESS_DISTRICT = "district_id";
            public static final String ADDRESS_LOCATION = "address";
            public static final String ADDRESS_ZIPCODE = "zipcode";
            public static final String ADDRESS_TEL = "tel";

            public static final String CONSUMPTION = "consumption";
            public static final String PROMOTION_ID = "promotion_id";
            public static final String PROMOTION_TYPE = "promotion_type";
            public static final String SECURITY_CODE = "security_code";
            public static final String ITEM_ID = "item_id";
            public static final String MIHOMEBUY_ID = "client_mihome_id";
            // update url
            public static final String REQUEST_DATA = "requestData";
            public static final String DATA = "data";
            public static final String PACKAGE = "package";
            public static final String VERSION = "version";
            // checkcode
            public static final String CHECKCODE_TYPE = "type";
            public static final String CHECKCODE_CODE = "checkcode";
            // fcode
            public static final String FCODE = "fcode";
            public static final String MIHOME_ID = "mihomeid";
            public static final String MI_PHONE_TYPE = "phone_type";
            public static final String ADAPT_PHONE = "adapt";
            public static final String ADAPT_SIMPLE = "simple";

            // Review
            public static final String GOODS_ID = "goods_id";
            public static final String QUALITY_GRADE = "quality_grade";
            public static final String SERVICE_GRADE = "service_grade";
            public static final String DELIVER_GRADE = "deliver_grade";
            public static final String COMMENT_CONTENT = "comment_content";
            // 米家可售库存
            public static final String PICKUP_ID = "pickup_id";
        }

        public static final class Values {
            // 每页请求数量
            public static final int PAGESIZE_VALUE = 20;
            // APP Id
            public static final String CLIENT_ID = "180100031022";
            // 支付宝支付途径
            public static final String ALIPAY_TYPE = "alipay";
            public static final String COOKIE_VALUE_PLATFROM = "platform";
            public static final String CHECKCODE_TYPE_GET = "get";
            public static final String CHECKCODE_TYPE_CHECK = "check";
            // 来源
            public static final String SOURCE_FCODE = "fcode";
            public static final String SOURCE_SHAKE = "ernie";
            /** from online, not from mi-home */
            public static final String MIHOME_BUY_NULL = "client_mihome_id_null";
            public static final String ADAPT_SIMPLE_VALUE = "2";
        }

    }

       public static boolean isTest = true;  //是否是测试环境
       public static final String URL_XMS_SALE_API = isTest ? "http://10.236.121.5:9000/saleapi" : "https://pos.be.xiaomi.com/saleapi";
       public static final String URL_M_XMS_SALE_API =  isTest ? "http://10.236.121.29/call/salesWriteLog" : "http://m.xms.be.xiaomi.com/call/salesWriteLog"; 

    public static final class Method {
        public static final String METHOD_GETUSERINFO = "sales.getUserInfo"; // 用户信息
        public static final String METHOD_GOODSLISTBYCATEID = "sales.getGoodsListByCateId"; // 分类商品信息列表
        public static final String METHOD_CARTMANAGEMENT = "sales.salesCartManagement"; // 购物车管理（修改、删除）
        public static final String METHOD_GETSALESCARTLIST = "sales.getSalesCartList"; // 查询购物车列表
        public static final String METHOD_CREATESALESORDE = "sales.createSalesOrder"; // 创建订单
        public static final String METHOD_GETSALESORDERLIST = "sales.getSalesOrderListByOrderStatus"; // 查询订单列表
        public static final String METHOD_GETSALESORDERINFO = "sales.getSalesOrderInfobyServiceNumber"; // 查询订单详情
        public static final String METHOD_CANCELSALESORDER = "sales.cancelSalesOrder"; // 查询订单详情
        public static final String METHOD_UPDATECONSIGNEEINF = "sales.updateConsigneeInfo"; // 更新订单的用户信息
        public static final String METHOD_GETCONSIGNEEINFO = "sales.getConsigneeInfoByServiceNumber"; // 获得订单的用户信息
        public static final String METHOD_GETSALESORDERCOUNT = "sales.getSalesOrderCount"; // 获得订单的用户信息
        public static final String METHOD_GETSTOCKNUM = "sales.getStocknumbyGoodsId"; // 根据goodid获取米家商品库存信息
        public static final String METHOD_SAVEPAYINFO = "pay.savePayInfo"; // 支付成功，更新服务器
        public static final String METHOD_REFUNDORDERO = "pay.refundOrder"; // 申请退款
        public static final String METHOD_GETBATCHEDVIEW = "sales.getBatchedViewByCommodityId"; // 读取套餐类型商品的定制信息
        public static final String METHOD_SEARCHGOODSLIST = "sales.searchGoodsListByKeyWord"; // 读取套餐类型商品的定制信息
        public static final String METHOD_GETEDITORDERLIST = "sales.getModifiableSalesOrderListByPage"; // 可修改的订单列表
        public static final String METHOD_ADDPRODUCTBYNFC = "sales.addDiscoveredNFCToShoppingCart"; // 读取NFC商品信息，并添加到购物车
        public static final String METHOD_WRITEPRODUCTTONFC = "sales.createNFCRecord"; // 写入商品信息到NFC标签
        public static final String METHOD_CHECKUPDATE = "sales.getLatestSalesApp"; // 检查版本更新
        public static final String METHOD_GETSKU = "sales.transformSKU";  //转为10位SKU
        public static final String METHOD_GETPRODUCTINFO = "sales.getProductInfo";  //获取商品信息
        public static final String METHOD_WIPEZERO = "sales.wipeZero";  //获取抹零后的总价
        public static final String METHOD_PACKED = "sales.packedAndOut";   //现货销售配货出库妥投/存储打单信息等
        public static final String METHOD_DEVICE = "sales.deviceList";   //获取打印机或POS机信息
        public static final String METHOD_SAVE_PRINTER = "sales.savePrinter"; //存储打印机信息
        public static final String METHOD_PAY_CHECK = "pay.payCheck";  //检查支付信息，主要是检测IP等或添加支付信息
        public static final String METHOD_UPLOAD_LOG = "sales.salesWriteLog"; //上传Log文件
        public static final String METHOD_GET_SERVICENUMBER = "sales.getSalesServiceNumber"; //获取订单号
        public static final String METHOD_DAYREFUNDCONFIRM = "pay.dayRefundConfirm"; // 当日退货确认
        public static final String METHOD_DAYREFUNDREQUEST = "pay.dayRefundRequest"; // 快速退货申请接口
        public static final String METHOD_DAYEXCHANGEREQUEST = "sales.replaceGoods"; // 快速换货接口
    }

    public static final class YeePay {
        public static final String SERVERIP = isTest ? "119.161.147.120" : "59.151.25.202"; 
        public static final int SERVERPORT = isTest ? 28100 : 28000;
    }

    public static final String TEST_SHOPAPI_ROOT = "http://shopapi.b2c.srv";   //测试的会有问题，用正式的
    public static final String FORMAL_SHOPAPI_ROOT = "http://shopapi.b2c.srv";
    public static final String URL_SHOPAPI_ROOT = ShopApp.isUserDebug() ? TEST_SHOPAPI_ROOT:FORMAL_SHOPAPI_ROOT;
    public static final String URL_SHOPAPI_PRODUCT = URL_SHOPAPI_ROOT +"/product/goodsView";
    public static final String URL_SHOPAPI_TRANSFORM = URL_SHOPAPI_ROOT +"/product/transform";
    
    
    public static final String TEST_URL_XIAOMI_M = "http://mtest.n.xiaomi.com/";
    public static final String TEST_URL_XIAOMI_SHOPAPI_ROOT = "http://test.app.shopapi.xiaomi.com/v2/";
    public static final String TEST_DOMAIN_APP_SHOPAPI = "app.shopapi.xiaomi.com";

    public static final String FORMAL_URL_XIAOMI_M = "http://m.xiaomi.com/";
    public static final String FORMAL_URL_XIAOMI_SHOPAPI_ROOT = "http://app.shopapi.xiaomi.com/v2/";
    public static final String FORMAL_DOMAIN_APP_SHOPAPI = "app.shopapi.xiaomi.com";

    // cookie form campaign webview
    public static final String DOMAIN_BASE = "xiaomi.com";
    public static final String DOMAIN_ACCOUNT = "account.xiaomi.com";
    public static final String DOMAIN_T_HD = "t.hd.xiaomi.com";
    public static final String DOMAIN_APP_SHOPAPI = ShopApp.isUserDebug() ? TEST_DOMAIN_APP_SHOPAPI
            : FORMAL_DOMAIN_APP_SHOPAPI;

    /** 各个业务的Authority **/
    public static final String URL_XIAOMI_M = ShopApp.isUserDebug() ? TEST_URL_XIAOMI_M
            : FORMAL_URL_XIAOMI_M;
    public static final String URL_XIAOMI_SHOPAPI_ROOT = ShopApp.isUserDebug() ? TEST_URL_XIAOMI_SHOPAPI_ROOT
            : FORMAL_URL_XIAOMI_SHOPAPI_ROOT;

    public static final String URL_UPDATE_BASE = "http://update.miui.com/updates/update_info/";
    public static final String URL_XIAOMI_SHOPAPI_PRODUCT = URL_XIAOMI_SHOPAPI_ROOT + "product/";
    public static final String URL_XIAOMI_SHOPAPI_PAY = URL_XIAOMI_SHOPAPI_ROOT + "pay/";
    // 商城问题反馈帖子
    public final static String URL_XIAOMI_SHOP_GUEST_URL = "http://bbs.xiaomi.cn/thread-5295957-1-1.html";
    // 小米手机立即购买跳转URL
    public final static String URL_XIAOMI_SHOP_MIPHONE_IMMEDIATELY_BUY_URL = "http://m.xiaomi.com/index.html#ac=home&op=selectversion&"
            + Parameters.Keys.MI_PHONE_TYPE + "=";
    // 支付失败可能原因说明URL
    public final static String URL_XIAOMI_SHOP_PAYMENT_EXPLAIN = "http://faq.xiaomi.cn/?note/view/19.html";
    /** 产品子业务路径 **/
    // http://app.shopapi.xiaomi.com/v2/apkversion/check?client_id=180100031013
    public static final String DIRECTORY_CHECK_UPDATE = "apkversion/check";
    public static final String DIRECTORY_UPDATE_INFO = "apkversion/info";
    public static final String DIRECTORY_CATEGORY = "category";
    public static final String DIRECTORY_CATEGORY_TREE = "categoryTree";
    public static final String DIRECTORY_USERINFO = "user/show";
    public static final String DIRECTORY_ADDRESSINFO_LIST = "address/list";
    public static final String DIRECTORY_ADDRESSINFO_DEL = "address/del";
    public static final String DIRECTORY_ADDRESSINFO_EDIT = "address/save";
    public static final String DIRECTORY_ADDRESSINFO_ADD = "address/add";

    public static final String DIRECTORY_PRODUCT = "allProductList";
    public static final String DIRECTORY_SHOPPING_CARTLIST = "shopping/cartList";
    public static final String DIRECTORY_SHOPPING_COUNT = "shopping/count";
    public static final String DIRECTORY_HOME_INDEX = "home/index";
    public static final String DIRECTORY_LOTTERY_INDEX = "activity/index";
    public static final String DIRECTORY_HOME_ACTIVITY_TYPE = "home/activityTypePhoto";
    public static final String DIRECTORY_PRODUCT_DETAILS = "view";
    public static final String DIRECTORY_ORDER_LIST = "order/listv2";
    public static final String DIRECTORY_ORDER_EDIT = "order/updateHandler";
    public static final String DIRECTORY_COMBO_LIST = "shopping/styleList";
    public static final String DIRECTORY_ORDER_VIEW = "order/viewv2";
    public static final String DIRECTORY_CHECKOUT = "order/checkout";
    public static final String DIRECTORY_ORDER_CANCEL = "order/cancel";
    public static final String DIRECTORY_CHECKOUT_SUBMIT = "order/getpayment";
    public static final String DIRECTORY_ORDER_SUBMIT = "order/submit";
    public static final String DIRECTORY_DELETE_CART = "shopping/delCart";
    public static final String DIRECTORY_EDIT_CONSUMPTION = "shopping/editConsumption";
    public static final String DIRECTORY_ADD_SHOPPING = "shopping/addCart";
    public static final String DIRECTORY_MUTI_ADD_SHOPPING = "shopping/multiAddCart";
    public static final String DIRECTORY_FCODE_CHECK = "mione/CheckFcode";
    public static final String DIRECTORY_ACTIVITY = "activity/control";
    public static final String DIRECTORY_CHECKCODE = "checkcode.php";
    public static final String DIRECTORY_XIAOMI_VIEW = "xiaomi/view";
    public static final String DIRECTORY_ADAPT_PHONE_LIST = "phoneAdapt";
    public static final String DIRECTORY_DEFENSE_HACKER_CHECKCODE = "checkcode/get";
    public static final String DIRECTORY_SERVICE_TOKENS = "eshop/list";
    public static final String DIRECTORY_SEND_MESSAGE = "pvcode/index";
    public static final String DIRECTORY_CHECK_MESSAGE_CODE = "pvcode/check";
    public static final String DIRECTORY_USER_REMIND = "user/remain";
    public static final String DIRECTORY_MIHOME_CANCELRESERVE = "mihome/cancelReserve";
    public static final String DIRECTORY_MIHOME_SIGNIN = "mihome/signin";
    public static final String DIRECTORY_MIHOME_SIGNIN_INFO = "mihome/signinfo";
    public static final String DIRECTORY_SHOW_CHAT = "helper/scale";
    public static final String DIRECTORY_SEARCHPRODUCT = "xmSearch";
    public static final String DIRECTORY_USER_SALE_OUT_REG = "user/saleoutReg";
    public static final String DIRECTORY_COMMENT_LIST = "comment/getList";
    public static final String DIRECTORY_COMMENT_DETAIL = "comment/getDetail";
    public static final String DIRECTORY_COMMENT_TOTAL = "comment/getTotal";
    public static final String DIRECTORY_COMMENT_GOODS_LIST = "comment/getGoodsList";
    public static final String DIRECTORY_COMMENT_ADD = "comment/add";
    public static final String DIRECTORY_ORDER_EXPRESS = "order/express";
    public static final String DIRECTORY_MIHOME_STORAGE = "xms/getstorage";
    public static final String DIRECTORY_USER_FAVORITELIST = "user/favoriteList";
    public static final String DIRECTORY_USER_FAVORITEADD = "user/favoriteAdd";
    public static final String DIRECTORY_USER_FAVORITEDROP = "user/favoriteDrop";
    public static final String DIRECTORY_PRODUCT_HOTSEARCH = "product/hotSearch";
    public static final String DIRECTORY_PRODUCT_EXPANDSEARCH = "product/expandSearch";
    public static final String DIRECTORY_SPLASH = "home/welcome";
    public static final String DIRECTORY_ORDER_REGION_PAYMENT = "order/getRegionPayment";
    public static final String DIRECTORY_PAYMENT_MODE = "banklist";
    public static final String DIRECTORY_RECOMMEND_PRODUCT = "relatedGoods";
    /** 支付业务子路径 **/

    public static final String DIRECTORY_ALIPAY = "alipay";
    public static final String DIRECTORY_SECURITY_PAY = "securitypay";
    public static final String URL_PAY_RESULT_PATH = "/api/bill/informchargeresult.do";
    public static final String DIRECTORY_UPPAY = "upmp_securitypay";

    /**
     * 获取更新日志的地址
     */
    public static String getUpdateLogURL(String version) {
        return String.format("%s%s?v=%s&%s=%s&%s=%s",
                URL_XIAOMI_SHOPAPI_ROOT, DIRECTORY_UPDATE_INFO,
                version,
                Parameters.Keys.CLIENT_ID, Parameters.Values.CLIENT_ID,
                Parameters.Keys.DEVICE_DENSITY, String.valueOf(Device.DISPLAY_DENSITY));
    }

    /**
     * 获取更新版本URL
     */
    public static String getUpdateURL() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_CHECK_UPDATE;
    }

    /**
     * 获取订单列表
     */
    public static String getOrderList() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_ORDER_LIST;
    }

    /**
     * 获取订单详情
     */
    public static String getOrderView() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_ORDER_VIEW;
    }

    /**
     * 取消订单
     */
    public static String getCancelOrder() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_ORDER_CANCEL;
    }

    /**
     * 获取分类列表URL
     * 
     * @return 下载分类列表的url
     */
    public static String getCategory() {
        return URL_XIAOMI_SHOPAPI_PRODUCT + DIRECTORY_CATEGORY;
    }

    public static String getCategoryTree() {
        return URL_XIAOMI_SHOPAPI_PRODUCT + DIRECTORY_CATEGORY_TREE;
    }

    /**
     * 获取分类下列表各商品URL
     * 
     * @return 下载分类列表下各商品的url
     */
    public static String getProduct() {
        return URL_XIAOMI_SHOPAPI_PRODUCT + DIRECTORY_PRODUCT;
    }

    public static String getPaymentMode() {
        return URL_XIAOMI_SHOPAPI_PAY + DIRECTORY_PAYMENT_MODE;
    }

    /**
     * 获取alipay的路径
     */
    public static String getAlipay() {
        return URL_XIAOMI_SHOPAPI_PAY + DIRECTORY_ALIPAY;
    }

    /**
     * 安全支付路径
     */
    public static String getSecurityPay() {
        return URL_XIAOMI_SHOPAPI_PAY + DIRECTORY_SECURITY_PAY;
    }

    /**
     * 银联支付
     */
    public static String getUPPaySecurityPay() {
        return URL_XIAOMI_SHOPAPI_PAY + DIRECTORY_UPPAY;
    }
    
    /**
     * 通过SHOPAPI查询商品信息
     */
    public static String getProductDetailsByShopApi(){
    	return URL_SHOPAPI_PRODUCT;
    }

    public static String getProductId(){
    	return URL_SHOPAPI_TRANSFORM;
    }
    /**
     * 获取商品详情的url
     * 
     * @return 商品详情的url
     */
    public static String getProductDetails() {
        return URL_XIAOMI_SHOPAPI_PRODUCT + DIRECTORY_PRODUCT_DETAILS;
    }

    public static String getRecommendProduct() {
        return URL_XIAOMI_SHOPAPI_PRODUCT + DIRECTORY_RECOMMEND_PRODUCT;
    }

    /**
     * 获取购物车列表
     * 
     * @return 购物车的url
     */
    public static String getShoppingCartList() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_SHOPPING_CARTLIST;
    }

    public static String getCheckout() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_CHECKOUT;
    }

    public static String getCheckoutSubmit() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_CHECKOUT_SUBMIT;
    }

    public static String getOrderSubmit() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_ORDER_SUBMIT;
    }

    /**
     * 获取购物车中商品数量
     * 
     * @return
     */
    public static String getShoppingCount() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_SHOPPING_COUNT;
    }

    /**
     * 获取用户个人信息
     */
    public static String getUserInfo() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_USERINFO;
    }

    /**
     * 获取首页数据
     */
    public static String getHome() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_HOME_INDEX;
    }

    /**
     * 获取活动页数据
     */
    public static String getLottery() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_LOTTERY_INDEX;
    }

    /**
     * 获取首页活动分类的小图
     */
    public static String getHomeActivityTypePhoto() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_HOME_ACTIVITY_TYPE;
    }

    /**
     * 获取套餐列表
     */
    public static String getComboList() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_COMBO_LIST;
    }

    /**
     * 加入购物车列表
     */
    public static String getAddShopping() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_ADD_SHOPPING;
    }

    /**
     * 加入多项购物车列表
     */
    public static String getMutiAddShopping() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_MUTI_ADD_SHOPPING;
    }

    /**
     * 存储打印信息
     * @return
     */
    public static String savePrinter(){
    	return "http://m.xms.be9.xiaomi.com/api/saveSalesPrintQueue";
    }
    
    /*
     * 获取个人地址列表
     */
    public static String getAddressInfo() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_ADDRESSINFO_LIST;
    }

    public static String getDelAddressInfo() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_ADDRESSINFO_DEL;
    }

    public static String getDeleteCart() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_DELETE_CART;
    }

    public static String getEditConsumption() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_EDIT_CONSUMPTION;
    }

    public static String getCheckFcode() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_FCODE_CHECK;
    }

    public static String getActivity() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_ACTIVITY;
    }

    public static String getCheckVCode() {
        return URL_XIAOMI_M + DIRECTORY_CHECKCODE;
    }

    public static String getMiPhoneDetail() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_XIAOMI_VIEW;
    }

    public static String getEditAddressInfo() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_ADDRESSINFO_EDIT;
    }

    public static String getAddAddressInfo() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_ADDRESSINFO_ADD;
    }

    public static String getAdaptPhoneInfo() {
        return URL_XIAOMI_SHOPAPI_PRODUCT + DIRECTORY_ADAPT_PHONE_LIST;
    }

    public static String getDefenseCheckCode() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_DEFENSE_HACKER_CHECKCODE;
    }

    public static String getServiceTokens() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_SERVICE_TOKENS;
    }

    public static String getRemindCount() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_USER_REMIND;
    }

    public static String getEditOrder() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_ORDER_EDIT;
    }

    public static String getSendMessage() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_SEND_MESSAGE;
    }

    public static String getCheckMessageCode() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_CHECK_MESSAGE_CODE;
    }

    public static String getCancelReserve() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_MIHOME_CANCELRESERVE;
    }

    public static String getMihomeSignin() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_MIHOME_SIGNIN;
    }

    public static String getMihomeSignInfo() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_MIHOME_SIGNIN_INFO;
    }

    public static String getShowChatList() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_SHOW_CHAT;
    }

    public static String getSearchProduct() {
        return URL_XIAOMI_SHOPAPI_PRODUCT + DIRECTORY_SEARCHPRODUCT;
    }

    public static String getSaleOutReg() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_USER_SALE_OUT_REG;
    }

    /**
     * Comment
     */
    public static String getCommentList() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_COMMENT_LIST;
    }

    public static String getCommentDetail() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_COMMENT_DETAIL;
    }

    public static String getCommentTotal() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_COMMENT_TOTAL;
    }

    public static String getCommentGoodsList() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_COMMENT_GOODS_LIST;
    }

    public static String getCommentAdd() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_COMMENT_ADD;
    }

    public static String getOrderExpress() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_ORDER_EXPRESS;
    }

    public static String getMihomeStorage() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_MIHOME_STORAGE;
    }

    public static String getFavoriteList() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_USER_FAVORITELIST;
    }

    public static String getFavoriteAdd() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_USER_FAVORITEADD;
    }

    public static String getFavoriteDrop() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_USER_FAVORITEDROP;
    }

    public static String getHotSearch() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_PRODUCT_HOTSEARCH;
    }

    public static String getExpandSearch() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_PRODUCT_EXPANDSEARCH;
    }

    public static String getSplash() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_SPLASH;
    }

    public static String getRegionPayment() {
        return URL_XIAOMI_SHOPAPI_ROOT + DIRECTORY_ORDER_REGION_PAYMENT;
    }

    /**
     * set cookie by default domain is .xiaomi.com
     */
    private static void setCookie(Context context, String name, String value) {
        setCookie(context, name, value, DOMAIN_BASE);
    }

    /**
     * set cookie for webView.
     * 
     * @see http://zlping.iteye.com/blog/1633213 webview管理cookies在各版本中的区别
     */
    public static void setCookie(Context context, String name, String value, String domain) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        if (cookieManager == null) {
            return;
        }
        String cookieString = name + "=" + value + "; domain=" + domain;
        cookieManager.setCookie(domain, cookieString);
        LogUtil.d(TAG, "set Cookie: " + domain);
        CookieSyncManager.getInstance().sync();
    }

    private static void removeCookie(Context context, String name, String domain) {
        LogUtil.d(TAG, "remove Cookie: " + domain + ": " + name);
        CookieManager cookieManager = CookieManager.getInstance();
        CookieSyncManager.createInstance(context);
        String cookies = cookieManager.getCookie(domain);
        if (cookies == null) {
            return;
        }
        for (String cookie : cookies.split(";")) {
            String[] cookieValues = cookie.split("=");
            if (cookieValues.length < 2) {
                return;
            }
            if (TextUtils.equals(cookieValues[0].trim(), name)) {
                StringBuilder cookieString = new StringBuilder();
                cookieString.append(name);
                cookieString.append("=;domain=");
                cookieString.append(domain);
                cookieString.append(";expires=-1");
                cookieManager.setCookie(domain, cookieString.toString());
                // cookieManager.removeExpiredCookie();
                CookieSyncManager.getInstance().sync();
            }
        }
    }

    public static void setLoginCookies(Context context) {
        // Remove all login cookies before setting.
        LoginManager loginManager = LoginManager.getInstance();
        if (loginManager.hasLogin()) {
            // 无论如何，都会将UserId种上
            HostManager.setCookie(context, HostManager.Parameters.Keys.USER_ID,
                    loginManager.getUserId());

            // 对于默认的SID，app有有效的过期重新获取机制
            String seviceToken = loginManager
                    .getExtendedAuthToken(Constants.Account.DEFAULT_SERVICE_ID) == null ? null
                    : loginManager.getExtendedAuthToken(Constants.Account.DEFAULT_SERVICE_ID).authToken;
            if (!TextUtils.isEmpty(seviceToken)) {
                HostManager.setCookie(context, HostManager.Parameters.Keys.SERVICE_TOKEN,
                        URLEncoder.encode(seviceToken), DOMAIN_APP_SHOPAPI);
            }

            String passToken = loginManager.getPassToken();
            if (!TextUtils.isEmpty(passToken)) {// 如果passToken不为空，那么只需要种passToken
                HostManager.setCookie(context, HostManager.Parameters.Keys.PASS_TOKEN,
                        passToken, DOMAIN_ACCOUNT);
            } else { // 如果没有passToken，那么需要将所有的serviceToken种上
                setWebRequiredServiceTokens(context);
            }
        }
    }

    private static void setWebRequiredServiceTokens(Context context) {
        LoginManager loginManager = LoginManager.getInstance();
        // 首先读取Cache中缓存的ServiceToken，以便立即更新到WebView上，防止出现本地登录而Web上没有登录的情况
        Map<String, ExtendedAuthToken> serviceTokens = loginManager
                .getWebRequiredCachedServiceTokens();
        setServiceTokenCookies(context, serviceTokens);

        // 为了适应业务的变化，读取服务端配置的最新ServiceToken信息，更新缓存。这种变化不是经常发生，即使上一步
        // 出现错误，当用户下次打开应用的时候就恢复正常了
        serviceTokens = loginManager.getWebRequiredServiceTokens();
        setServiceTokenCookies(context, serviceTokens);
    }

    private static void setServiceTokenCookies(Context context,
            Map<String, ExtendedAuthToken> serviceTokens) {
        if (serviceTokens != null) {
            Iterator<Entry<String, ExtendedAuthToken>> iterator = serviceTokens.entrySet()
                    .iterator();
            while (iterator.hasNext()) {
                Entry<String, ExtendedAuthToken> entry = iterator.next();
                setCookie(context, HostManager.Parameters.Keys.SERVICE_TOKEN,
                        URLEncoder.encode(entry.getValue().authToken), entry.getKey());
            }
        }
    }

    public static void removeLoginCookies(Context context) {
        /**
         * @HACKME 
         *         removeCookie方法无法移除“account.xiaomi.com”（不是“.account.xiaomi.com”
         *         ）下的passToken值。 这个值是https://account.xiaomi.com/pass/
         *         serviceLogin自己种进去的。因此，采用全清-重新种cookie的方法。
         */
        LogUtil.d(TAG, "remove login cookie");
        CookieManager.getInstance().removeSessionCookie();
        HostManager.removeCookie(context, HostManager.Parameters.Keys.SERVICE_TOKEN,
                DOMAIN_T_HD);
        CookieSyncManager.getInstance().sync();
        initSettingCookies(context);
    }

    public static void initSettingCookies(Context context) {
        HostManager.setCookie(context,
                HostManager.Parameters.Keys.COOKIE_NAME_PLATFORM,
                HostManager.Parameters.Values.COOKIE_VALUE_PLATFROM);
    }
}
