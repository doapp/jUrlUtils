package com.doapps.utils.url;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModifiableURL {

  private String urlPath = null;
  private Map<String, String> urlEncodedParams = new HashMap<String, String>();
  private Map<String, String> urlParamsToEncode = new HashMap<String, String>();
  private String builtUrl = null;

  private static final char GET_ARG_START = '?';
  private static final char ARG_EQUALS = '=';
  private static final char GET_ARG_SEP = '&';
  private static final String UTF8 = "UTF-8"; //$NON-NLS-1$

  // this regex matches url params in group 1 and values in group 2
  private static final String URL_QUERY_REGEX = "[\\?&]([^&=]+)=([^&=]+)"; //$NON-NLS-1$
  private static final Pattern urlQueryPattern = Pattern.compile(URL_QUERY_REGEX);

  public static ModifiableURL parse(String url) throws MalformedURLException {
    if (url == null || url.length() == 0) {
      return null;
    }

    ModifiableURL result = new ModifiableURL();

    if (url.contains("?")) {
      //has query to parse
      Matcher matcher = urlQueryPattern.matcher(url);
      if (matcher.find()) {
        //found params
        result.urlPath = url.substring(0, url.indexOf("?"));
        recordFoundParams(matcher, result);
        while (matcher.find()) {
          recordFoundParams(matcher, result);
        }
      }
    } else {
      //no query to parse
      result.urlPath = url;
    }

    return result;
  }
  private static void recordFoundParams(Matcher matcher, ModifiableURL result) {
    String key = matcher.group(1);
    String value = matcher.group(2);
    result.urlEncodedParams.put(key, value);
  }

  private ModifiableURL() {
    // hide this
  }

  /**
   * Adds a parameter to the url.  <s>This value will be encoded and this replaces the previously added value</s>
   *
   * @param key   parameter name
   * @param value parameter value
   */
  public void addParam(String key, String value) {
    urlParamsToEncode.put(key, value);
    urlEncodedParams.remove(key);
  }

  private void buildUrl() {
    StringBuilder buffer = new StringBuilder();

    buffer.append(urlPath);

    if (!urlParamsToEncode.isEmpty()) {
      try {
        encodePendingParams();
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
        urlParamsToEncode.clear();
      }
    }

    if (urlEncodedParams.size() > 0) {
      buffer.append(GET_ARG_START);
      Iterator<Entry<String, String>> iterator = urlEncodedParams.entrySet().iterator();
      while (iterator.hasNext()) {
        Entry<String, String> entry = iterator.next();
        buffer.append(entry.getKey());
        buffer.append(ARG_EQUALS);
        buffer.append(entry.getValue());
        if (iterator.hasNext()) {
          buffer.append(GET_ARG_SEP);
        }
      }
    }

    builtUrl = buffer.toString();
  }
  private void encodePendingParams() throws UnsupportedEncodingException {
    for (Entry<String, String> entry : urlParamsToEncode.entrySet()) {
      String enc_val = URLEncoder.encode(entry.getValue(), UTF8);
      urlEncodedParams.put(entry.getKey(), enc_val);
    }
    urlParamsToEncode.clear();
  }

  public Map<String, String> getUrlParams() {
    Map<String, String> result = Maps.newHashMap(urlEncodedParams);
    result.putAll(urlParamsToEncode);
    return result;
  }

  public void replaceFirstMatchingValue(String token, String replacement) {
    if (token == null || replacement == null) {
      return;
    }

    Optional<String> key = findKeyByValue(token, urlEncodedParams);
    if (key.isPresent()) {
      urlEncodedParams.remove(key.get());
      urlParamsToEncode.put(key.get(), replacement);
    }

    key = findKeyByValue(token, urlParamsToEncode);
    if (key.isPresent()) {
      urlParamsToEncode.put(key.get(), replacement);
    }
  }
  private Optional<String> findKeyByValue(String token, Map<String, String> map) {
    String replacedKey = null;
    if (map.containsValue(token)) {
      for (Entry<String, String> entry : map.entrySet()) {
        if (token.equals(entry.getValue())) {
          replacedKey = entry.getKey();
        }
      }
    }
    return Optional.fromNullable(replacedKey);
  }

  @Override
  public String toString() {
    //we only rebuild if there is pending parameters to encode
    if (builtUrl == null || !urlParamsToEncode.isEmpty()) {
      buildUrl();
    }
    return builtUrl;
  }

  public static Optional<ModifiableURL> tryParse(String url) {
    ModifiableURL modifiableURL = null;
    try {
      modifiableURL = ModifiableURL.parse(url);
    } catch (Throwable ignored) {
    }
    return Optional.fromNullable(modifiableURL);
  }
}
