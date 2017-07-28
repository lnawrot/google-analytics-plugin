package com.danielcwilson.plugins.analytics;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger.LogLevel;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.analytics.ecommerce.Product;
import com.google.android.gms.analytics.ecommerce.ProductAction;
import com.google.android.gms.analytics.ecommerce.Promotion;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map.Entry;

public class UniversalAnalyticsPlugin extends CordovaPlugin {
    public static final String START_TRACKER = "startTrackerWithId";
    public static final String TRACK_VIEW = "trackView";
    public static final String TRACK_EVENT = "trackEvent";
    public static final String TRACK_EXCEPTION = "trackException";
    public static final String TRACK_TIMING = "trackTiming";
    public static final String TRACK_METRIC = "trackMetric";
    public static final String ADD_DIMENSION = "addCustomDimension";
    public static final String ADD_TRANSACTION = "addTransaction";
    public static final String ADD_TRANSACTION_ITEM = "addTransactionItem";
    public static final String ADD_IMPRESION = "addImpression";
    public static final String PRODUCT_ACTION = "productAction";
    public static final String ADD_PROMOTION = "addPromotion";

    public static final String SET_ALLOW_IDFA_COLLECTION = "setAllowIDFACollection";
    public static final String SET_USER_ID = "setUserId";
    public static final String SET_ANONYMIZE_IP = "setAnonymizeIp";
    public static final String SET_OPT_OUT = "setOptOut";
    public static final String SET_APP_VERSION = "setAppVersion";
    public static final String GET_VAR = "getVar";
    public static final String SET_VAR = "setVar";
    public static final String DISPATCH = "dispatch";
    public static final String DEBUG_MODE = "debugMode";
    public static final String ENABLE_UNCAUGHT_EXCEPTION_REPORTING = "enableUncaughtExceptionReporting";

    public Boolean trackerStarted = false;
    public Boolean debugModeEnabled = false;
    public HashMap<Integer, String> customDimensions = new HashMap<Integer, String>();
    public HashMap<Integer, Float> customMetrics = new HashMap<Integer, Float>();

    public Tracker tracker;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (START_TRACKER.equals(action)) {
            String id = args.getString(0);
            int dispatchPeriod = args.length() > 1 ? args.getInt(1) : 30;
            this.startTracker(id, dispatchPeriod, callbackContext);
            return true;
        } else if (TRACK_VIEW.equals(action)) {
            int length = args.length();
            String screen = args.getString(0);
            this.trackView(screen, length > 1 && !args.isNull(1) ? args.getString(1) : "",
                    length > 2 && !args.isNull(2) ? args.getBoolean(2) : false, callbackContext);
            return true;
        } else if (TRACK_EVENT.equals(action)) {
            int length = args.length();
            if (length > 0) {
                this.trackEvent(args.getString(0), 
                        length > 1 ? args.getString(1) : "",
                        length > 2 ? args.getString(2) : "", 
                        length > 3 ? args.getLong(3) : 0,
                        length > 4 ? args.getBoolean(4) : false, callbackContext);
            }
            return true;
        } else if (TRACK_EXCEPTION.equals(action)) {
            String description = args.getString(0);
            Boolean fatal = args.getBoolean(1);
            this.trackException(description, fatal, callbackContext);
            return true;
        } else if (TRACK_TIMING.equals(action)) {
            int length = args.length();
            if (length > 0) {
                this.trackTiming(args.getString(0), 
                        length > 1 ? args.getLong(1) : 0,
                        length > 2 ? args.getString(2) : "", 
                        length > 3 ? args.getString(3) : "", callbackContext);
            }
            return true;
        } else if (TRACK_METRIC.equals(action)) {
            int length = args.length();
            if (length > 0) {
                this.trackMetric(args.getInt(0), length > 1 ? args.getString(1) : "", callbackContext);
            }
            return true;
        } else if (ADD_DIMENSION.equals(action)) {
            Integer key = args.getInt(0);
            String value = args.getString(1);
            this.addCustomDimension(key, value, callbackContext);
            return true;
        } else if (ADD_TRANSACTION.equals(action)) {
            int length = args.length();
            if (length > 0) {
                this.addTransaction(args.getString(0), length > 1 ? args.getString(1) : "",
                        length > 2 ? args.getDouble(2) : 0, 
                        length > 3 ? args.getDouble(3) : 0,
                        length > 4 ? args.getDouble(4) : 0, 
                        length > 5 ? args.getString(5) : null, callbackContext);
            }
            return true;
        } else if (ADD_TRANSACTION_ITEM.equals(action)) {
            int length = args.length();
            if (length > 0) {
                this.addTransactionItem(args.getString(0), length > 1 ? args.getString(1) : "",
                        length > 2 ? args.getString(2) : "", 
                        length > 3 ? args.getString(3) : "",
                        length > 4 ? args.getDouble(4) : 0, 
                        length > 5 ? args.getLong(5) : 0,
                        length > 6 ? args.getString(6) : null, callbackContext);
            }
            return true;
        } else if (ADD_IMPRESION.equals(action)) {
            this.addImpresion(args.getString(0), args.getJSONObject(1), callbackContext);

            return true;
        } else if (PRODUCT_ACTION.equals(action)) {
            this.productAction(args.getString(0), args.getJSONObject(1), args.getJSONObject(2), callbackContext);
            return true;
        } else if (ADD_PROMOTION.equals(action)) {
            int length = args.length();
            this.addPromotion(args.getString(0), args.getJSONObject(1), 
                                    length > 2 ? args.getString(2) : "", 
                                    length > 3 ? args.getString(3) : "", callbackContext);
            return true;
        } else if (SET_ALLOW_IDFA_COLLECTION.equals(action)) {
            this.setAllowIDFACollection(args.getBoolean(0), callbackContext);
        } else if (SET_USER_ID.equals(action)) {
            String userId = args.getString(0);
            this.setUserId(userId, callbackContext);
        } else if (SET_ANONYMIZE_IP.equals(action)) {
            boolean anonymize = args.getBoolean(0);
            this.setAnonymizeIp(anonymize, callbackContext);
        } else if (SET_OPT_OUT.equals(action)) {
            boolean optout = args.getBoolean(0);
            this.setOptOut(optout, callbackContext);
        } else if (SET_APP_VERSION.equals(action)) {
            String version = args.getString(0);
            this.setAppVersion(version, callbackContext);
        } else if (GET_VAR.equals(action)) {
            String variable = args.getString(0);
            this.getVar(variable, callbackContext);
        } else if (SET_VAR.equals(action)) {
            String variable = args.getString(0);
            String value = args.getString(1);
            this.setVar(variable, value, callbackContext);
            return true;
        } else if (DISPATCH.equals(action)) {
            this.dispatch(callbackContext);
            return true;
        } else if (DEBUG_MODE.equals(action)) {
            this.debugMode(callbackContext);
        } else if (ENABLE_UNCAUGHT_EXCEPTION_REPORTING.equals(action)) {
            Boolean enable = args.getBoolean(0);
            this.enableUncaughtExceptionReporting(enable, callbackContext);
        }
        return false;
    }

    private void startTracker(String id, int dispatchPeriod, CallbackContext callbackContext) {
        if (null != id && id.length() > 0) {
            tracker = GoogleAnalytics.getInstance(this.cordova.getActivity()).newTracker(id);
            callbackContext.success("tracker started");
            trackerStarted = true;
            GoogleAnalytics.getInstance(this.cordova.getActivity()).setLocalDispatchPeriod(dispatchPeriod);
        } else {
            callbackContext.error("tracker id is not valid");
        }
    }

    private void addCustomDimension(Integer key, String value, CallbackContext callbackContext) {
        if (key <= 0) {
            callbackContext.error("Expected positive integer argument for key.");
            return;
        }

        if (null == value || value.length() == 0) {
            // unset dimension
            customDimensions.remove(key);
            callbackContext.success("custom dimension stopped");    
        } else {
            customDimensions.put(key, value);
            callbackContext.success("custom dimension started");
        }
    }

    private <T> void addCustomDimensionsAndMetricsToHitBuilder(T builder) {
        //unfortunately the base HitBuilders.HitBuilder class is not public, therefore have to use reflection to use
        //the common setCustomDimension (int index, String dimension) and setCustomMetrics (int index, Float metric) methods
        try {
            Method builderMethod = builder.getClass().getMethod("setCustomDimension", Integer.TYPE, String.class);

            for (Entry<Integer, String> entry : customDimensions.entrySet()) {
                Integer key = entry.getKey();
                String value = entry.getValue();
                try {
                    builderMethod.invoke(builder, (key), value);
                } catch (IllegalArgumentException e) {
                } catch (IllegalAccessException e) {
                } catch (InvocationTargetException e) {
                }
            }
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        }

        try {
            Method builderMethod = builder.getClass().getMethod("setCustomMetric", Integer.TYPE, Float.TYPE);

            for (Entry<Integer, Float> entry : customMetrics.entrySet()) {
                Integer key = entry.getKey();
                Float value = entry.getValue();
                try {
                    builderMethod.invoke(builder, (key), value);
                } catch (IllegalArgumentException e) {
                } catch (IllegalAccessException e) {
                } catch (InvocationTargetException e) {
                }
            }
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        }
    }

    private void trackView(String screenname, String campaignUrl, boolean newSession, CallbackContext callbackContext) {
        if (!trackerStarted) {
            callbackContext.error("Tracker not started");
            return;
        }

        if (null != screenname && screenname.length() > 0) {
            tracker.setScreenName(screenname);

            HitBuilders.ScreenViewBuilder hitBuilder = new HitBuilders.ScreenViewBuilder();
            addCustomDimensionsAndMetricsToHitBuilder(hitBuilder);

            if (!campaignUrl.equals("")) {
                hitBuilder.setCampaignParamsFromUrl(campaignUrl);
            }

            if (!newSession) {
                tracker.send(hitBuilder.build());
            } else {
                tracker.send(hitBuilder.setNewSession().build());
            }

            callbackContext.success("Track Screen: " + screenname);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void trackEvent(String category, String action, String label, long value, boolean newSession,
            CallbackContext callbackContext) {
        if (!trackerStarted) {
            callbackContext.error("Tracker not started");
            return;
        }

        if (null != category && category.length() > 0) {
            HitBuilders.EventBuilder hitBuilder = new HitBuilders.EventBuilder();
            addCustomDimensionsAndMetricsToHitBuilder(hitBuilder);

            if (!newSession) {
                tracker.send(
                        hitBuilder.setCategory(category).setAction(action).setLabel(label).setValue(value).build());
            } else {
                tracker.send(hitBuilder.setCategory(category).setAction(action).setLabel(label).setValue(value)
                        .setNewSession().build());
            }

            callbackContext.success("Track Event: " + category);
        } else {
            callbackContext.error("Expected non-empty string arguments.");
        }
    }

    private void trackMetric(Integer key, String value, CallbackContext callbackContext) {
        if (key <= 0) {
            callbackContext.error("Expected positive integer argument for key.");
            return;
        }

        if (null == value || value.length() == 0) {
            // unset metric
            customMetrics.remove(key);
            callbackContext.success("custom metric stopped");
        } else {
            Float floatValue;
            try {
                floatValue = Float.parseFloat(value);
            } catch (NumberFormatException e) {
                callbackContext.error("Expected string formatted number for value.");
                return;
            }

            customMetrics.put(key, floatValue);
            callbackContext.success("custom metric started");
        }
    }

    private void trackException(String description, Boolean fatal, CallbackContext callbackContext) {
        if (!trackerStarted) {
            callbackContext.error("Tracker not started");
            return;
        }

        if (null != description && description.length() > 0) {
            HitBuilders.ExceptionBuilder hitBuilder = new HitBuilders.ExceptionBuilder();
            addCustomDimensionsAndMetricsToHitBuilder(hitBuilder);

            tracker.send(hitBuilder.setDescription(description).setFatal(fatal).build());
            callbackContext.success("Track Exception: " + description);
        } else {
            callbackContext.error("Expected non-empty string arguments.");
        }
    }

    private void trackTiming(String category, long intervalInMilliseconds, String name, String label,
            CallbackContext callbackContext) {
        if (!trackerStarted) {
            callbackContext.error("Tracker not started");
            return;
        }

        if (null != category && category.length() > 0) {
            HitBuilders.TimingBuilder hitBuilder = new HitBuilders.TimingBuilder();
            addCustomDimensionsAndMetricsToHitBuilder(hitBuilder);

            tracker.send(hitBuilder.setCategory(category).setValue(intervalInMilliseconds).setVariable(name)
                    .setLabel(label).build());
            callbackContext.success("Track Timing: " + category);
        } else {
            callbackContext.error("Expected non-empty string arguments.");
        }
    }

    private void addTransaction(String id, String affiliation, double revenue, double tax, double shipping,
            String currencyCode, CallbackContext callbackContext) {
        if (!trackerStarted) {
            callbackContext.error("Tracker not started");
            return;
        }

        if (null != id && id.length() > 0) {
            HitBuilders.TransactionBuilder hitBuilder = new HitBuilders.TransactionBuilder();
            addCustomDimensionsAndMetricsToHitBuilder(hitBuilder);

            tracker.send(hitBuilder.setTransactionId(id).setAffiliation(affiliation).setRevenue(revenue).setTax(tax)
                    .setShipping(shipping).setCurrencyCode(currencyCode).build()); //Deprecated
            callbackContext.success("Add Transaction: " + id);
        } else {
            callbackContext.error("Expected non-empty ID.");
        }
    }

    private void addTransactionItem(String id, String name, String sku, String category, double price, long quantity,
            String currencyCode, CallbackContext callbackContext) {
        if (!trackerStarted) {
            callbackContext.error("Tracker not started");
            return;
        }

        if (null != id && id.length() > 0) {
            HitBuilders.ItemBuilder hitBuilder = new HitBuilders.ItemBuilder();
            addCustomDimensionsAndMetricsToHitBuilder(hitBuilder);

            tracker.send(hitBuilder.setTransactionId(id).setName(name).setSku(sku).setCategory(category).setPrice(price)
                    .setQuantity(quantity).setCurrencyCode(currencyCode).build()); //Deprecated
            callbackContext.success("Add Transaction Item: " + id);
        } else {
            callbackContext.error("Expected non-empty ID.");
        }
    }

    private Product checkProduct(JSONObject productInput) {
        Product product = new Product();
        try {
            product.setId(productInput.getString("id"));
        } catch (JSONException ex) {
            // do nothing
        }
        try {
            product.setName(productInput.getString("name"));
        } catch (JSONException ex) {
            // do nothing
        }
        try {
            product.setCategory(productInput.getString("category"));
        } catch (JSONException ex) {
            // do nothing
        }
        try {
            product.setBrand(productInput.getString("brand"));

        } catch (JSONException ex) {
            // do nothing
        }
        try {
            product.setVariant(productInput.getString("variant"));
        } catch (JSONException ex) {
            // do nothing
        }
        try {
            product.setPosition(productInput.getInt("postition"));
        } catch (JSONException ex) {
            // do nothing
        }
        try {
            product.setCustomDimension(productInput.getJSONArray("customDimension").getInt(0), productInput.getJSONArray("customDimension").getString(1));            
        } catch (JSONException ex) {
            // do nothing
        }
        try {
            product.setPrice(productInput.getLong("price"));
        } catch (JSONException ex) {
            // do nothing
        }                                                
        try {
            product.setQuantity(productInput.getInt("quantity"));
        } catch (JSONException ex) {
            // do nothing
        }       
        try {
            product.setCouponCode(productInput.getString("couponCode"));
        } catch (JSONException ex) {
            // do nothing
        }              

        return product;
    }

    /**
     * Product Action definitions:
     *   ACTION_ADD Action to use when a product is added to the cart.
     *   ACTION_CHECKOUT Action to use for hits with checkout data.
     *   ACTION_CHECKOUT_OPTION Action to be used for supplemental checkout data that needs to be provided after a checkout hit.
     *   ACTION_CHECKOUT_OPTIONS This constant was deprecated. Use ACTION_CHECKOUT_OPTION instead.
     *   ACTION_CLICK Action to use when the user clicks on a set of products.
     *   ACTION_DETAIL Action to use when the user views detailed descriptions of products.
     *   ACTION_PURCHASE Action that is used to report all the transaction data to GA.
     *   ACTION_REFUND Action to use while reporting refunded transactions to GA.
     *   ACTION_REMOVE Action to use when a product is removed from the cart.
     */
    private ProductAction checkProductAction(JSONObject productActionInput) {
        ProductAction productAction = null;
        try {        
            if(productActionInput.getString("action").equals("ACTION_ADD")) {
                productAction = new ProductAction(ProductAction.ACTION_ADD);
            } else if(productActionInput.getString("action").equals("ACTION_CHECKOUT")) {
                productAction = new ProductAction(ProductAction.ACTION_CHECKOUT);
            } else if(productActionInput.getString("action").equals("ACTION_CHECKOUT_OPTION")) {
                productAction = new ProductAction(ProductAction.ACTION_CHECKOUT_OPTION);
            } else if(productActionInput.getString("action").equals("ACTION_CHECKOUT_OPTIONS")) {
                productAction = new ProductAction(ProductAction.ACTION_CHECKOUT_OPTIONS);
            } else if(productActionInput.getString("action").equals("ACTION_CLICK")) {
                productAction = new ProductAction(ProductAction.ACTION_CLICK);
            } else if(productActionInput.getString("action").equals("ACTION_DETAIL")) {
                productAction = new ProductAction(ProductAction.ACTION_DETAIL);
            } else if(productActionInput.getString("action").equals("ACTION_PURCHASE")) {
                productAction = new ProductAction(ProductAction.ACTION_PURCHASE);
            } else if(productActionInput.getString("action").equals("ACTION_REFUND")) {
                productAction = new ProductAction(ProductAction.ACTION_REFUND);
            } else if(productActionInput.getString("action").equals("ACTION_REMOVE")) {
                productAction = new ProductAction(ProductAction.ACTION_REMOVE);
            }
        } catch (JSONException ex) {
            // do nothing
        }  
        try {        
            productAction.setTransactionId(productActionInput.getString("transactionId"));
        } catch (JSONException ex) {
            // do nothing
        }            
        try {        
            productAction.setTransactionAffiliation(productActionInput.getString("transactionAffiliation"));
        } catch (JSONException ex) {
            // do nothing
        }  
        try {        
            productAction.setTransactionRevenue(productActionInput.getLong("transactionRevenue"));
        } catch (JSONException ex) {
            // do nothing
        }  
        try {        
            productAction.setTransactionTax(productActionInput.getLong("transactionTax"));
        } catch (JSONException ex) {
            // do nothing
        }  
        try {        
            productAction.setTransactionShipping(productActionInput.getLong("transactionShipping"));
        } catch (JSONException ex) {
            // do nothing
        }  
        try {        
            productAction.setTransactionCouponCode(productActionInput.getString("transactionCouponCode"));
        } catch (JSONException ex) {
            // do nothing
        }  
        try {        
            productAction.setCheckoutStep(productActionInput.getInt("checkoutStep"));
        } catch (JSONException ex) {
            // do nothing
        }                                                  

        return productAction;
    }
    /**
     * Promotion definitions:
        ACTION_CLICK Action to use when the user clicks/taps on a promotion.
        ACTION_VIEW Action to use when the user views a promotion.

        var promotion = {};
        promotion.id = "PROMO-ID1234";
        promotion.name = "mypromo";
        promotion.position = "position";
        promotion.creative = "creative";
     */
    private Promotion checkPromotion(JSONObject promotionInput) {
        Promotion promotion = new Promotion();

        try {        
            promotion.setId(promotionInput.getString("id"));
        } catch (JSONException ex) {
            // do nothing
        }            
        try {        
            promotion.setName(promotionInput.getString("name"));
        } catch (JSONException ex) {
            // do nothing
        }  
        try {        
            promotion.setPosition(promotionInput.getString("position"));
        } catch (JSONException ex) {
            // do nothing
        }  
        try {        
            promotion.setCreative(promotionInput.getString("creative"));
        } catch (JSONException ex) {
            // do nothing
        }                                                  

        return promotion;
    }

    private void addImpresion(String screenName, JSONObject productInput, CallbackContext callbackContext) {
        if (!trackerStarted) {
            callbackContext.error("Tracker not started");
            return;
        }

        Product product = checkProduct(productInput);
        HitBuilders.ScreenViewBuilder builder = new HitBuilders.ScreenViewBuilder()
            .addImpression(product, screenName);

        tracker.setScreenName(screenName);
        tracker.send(builder.build());   
        callbackContext.success("impresion action executed");                   
    }

    private void productAction(String screenName, JSONObject productInput, JSONObject productActionInput, CallbackContext callbackContext) {
        if (!trackerStarted) {
            callbackContext.error("Tracker not started");
            return;
        }

        Product product = checkProduct(productInput);
        ProductAction productAction = checkProductAction(productActionInput);

        HitBuilders.ScreenViewBuilder builder = new HitBuilders.ScreenViewBuilder()
            .addProduct(product)
            .setProductAction(productAction);

        tracker.setScreenName("transaction");
        tracker.send(builder.build());     

        callbackContext.success("product action executed");      
    }
    
    private void addPromotion(String action, JSONObject promotionInput, String category, String label, CallbackContext callbackContext) {
        if (!trackerStarted) {
            callbackContext.error("Tracker not started");
            return;
        }

        Promotion promotion = checkPromotion(promotionInput);
        HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder()
            .addPromotion(promotion)
            .setCategory(category)
            .setAction(action)
            .setLabel(label);
        
       
        if(action.equals("ACTION_CLICK")) {
            builder.setPromotionAction(Promotion.ACTION_CLICK);
        } else if(action.equals("ACTION_VIEW")) {
            builder.setPromotionAction(Promotion.ACTION_VIEW);
        } 

        tracker.send(builder.build());        

        callbackContext.success("promotion action executed");      
    }    

    private void setAllowIDFACollection(Boolean enable, CallbackContext callbackContext) {
        if (!trackerStarted) {
            callbackContext.error("Tracker not started");
            return;
        }

        tracker.enableAdvertisingIdCollection(enable);
        callbackContext.success("Enable Advertising Id Collection: " + enable);
    }

    private void setVar(String variable, String value, CallbackContext callbackContext) {
        if (!trackerStarted) {
            callbackContext.error("Tracker not started");
            return;
        }

        tracker.set(variable, value);
        callbackContext.success("Set variable " + variable + "to " + value);
    }

    private void dispatch(CallbackContext callbackContext) {
        if (!trackerStarted) {
            callbackContext.error("Tracker not started");
            return;
        }

        GoogleAnalytics.getInstance(this.cordova.getActivity()).dispatchLocalHits();
        callbackContext.success("dispatch sent");
    }

    private void getVar(String variable, CallbackContext callbackContext) {
        if (!trackerStarted) {
            callbackContext.error("Tracker not started");
            return;
        }

        String result = tracker.get(variable);
        callbackContext.success(result);
    }

    private void debugMode(CallbackContext callbackContext) {
        // GAv4 Logger is deprecated!
        // GoogleAnalytics.getInstance(this.cordova.getActivity()).getLogger().setLogLevel(LogLevel.VERBOSE);

        // To enable verbose logging execute "adb shell setprop log.tag.GAv4 DEBUG"
        // and then "adb logcat -v time -s GAv4" to inspect log entries.
        GoogleAnalytics.getInstance(this.cordova.getActivity()).setDryRun(true);

        this.debugModeEnabled = true;
        callbackContext.success("debugMode enabled");
    }

    private void setAnonymizeIp(boolean anonymize, CallbackContext callbackContext) {
        if (!trackerStarted) {
            callbackContext.error("Tracker not started");
            return;
        }

        tracker.setAnonymizeIp(anonymize);
        callbackContext.success("Set AnonymizeIp " + anonymize);
    }

    private void setOptOut(boolean optout, CallbackContext callbackContext) {
        if (!trackerStarted) {
            callbackContext.error("Tracker not started");
            return;
        }

        GoogleAnalytics.getInstance(this.cordova.getActivity()).setAppOptOut(optout);
        callbackContext.success("Set Opt-Out " + optout);
    }

    private void setUserId(String userId, CallbackContext callbackContext) {
        if (!trackerStarted) {
            callbackContext.error("Tracker not started");
            return;
        }

        tracker.set("&uid", userId);
        callbackContext.success("Set user id" + userId);
    }

    private void setAppVersion(String version, CallbackContext callbackContext) {
        if (!trackerStarted) {
            callbackContext.error("Tracker not started");
            return;
        }

        tracker.set("&av", version);
        callbackContext.success("Set app version: " + version);
    }

    private void enableUncaughtExceptionReporting(Boolean enable, CallbackContext callbackContext) {
        if (!trackerStarted) {
            callbackContext.error("Tracker not started");
            return;
        }

        tracker.enableExceptionReporting(enable);
        callbackContext.success((enable ? "Enabled" : "Disabled") + " uncaught exception reporting");
    }
}
