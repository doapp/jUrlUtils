package com.doapps.utils.url;

import com.google.common.base.Optional;

import org.hamcrest.MatcherAssert;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ModifiableURLTest {

  @org.junit.Test
  public void testUrlParseWithoutParams() throws Exception {
    ModifiableURL url = ModifiableURL.parse("http://www.google.com");
    assertThat(url.toString(), equalTo("http://www.google.com"));
    assertThat(url.getUrlParams().size(), equalTo(0));
  }

  @org.junit.Test
  public void testUrlParseFailure() throws Exception {
    ModifiableURL url = ModifiableURL.parse("com");
    assertThat(url,notNullValue());
  }

  @org.junit.Test
  public void testUrlParseWithParams() throws Exception {
    ModifiableURL url = ModifiableURL.parse("http://www.google.com?a=123&b=456");
    assertThat(url.toString(), startsWith("http://www.google.com?"));
    assertThat(url.toString(), containsString("a=123"));
    assertThat(url.toString(), containsString("b=456"));
    assertThat(url.getUrlParams().size(), equalTo(2));
    assertThat(url.getUrlParams(), hasEntry("a", "123"));
    assertThat(url.getUrlParams(), hasEntry("b", "456"));
  }

  @org.junit.Test
  public void testUrlParseWithModifiedParams() throws Exception {
    ModifiableURL url = ModifiableURL.parse("http://www.google.com?a=123&b=456");
    assertThat(url.getUrlParams().size(), equalTo(2));
    url.addParam("c", "789");
    url.addParam("a", "111");
    assertThat(url.toString(), startsWith("http://www.google.com?"));
    assertThat(url.toString(), containsString("a=111"));
    assertThat(url.toString(), containsString("c=789"));
    assertThat(url.toString(), containsString("b=456"));
    assertThat(url.getUrlParams().size(), equalTo(3));
  }
  @org.junit.Test
  public void testUrlParseWithPreEncodedParams() throws Exception {
    ModifiableURL url = ModifiableURL.parse("http://www.google.com?a=123&b=456&c=123,456,789");
    assertThat(url.getUrlParams().size(), equalTo(3));
    assertThat(url.toString(), containsString("c=123,456,789"));

    url = ModifiableURL.parse("http://www.google.com?a=123&b=456&c=123 456 789");
    assertThat(url.getUrlParams().size(), equalTo(3));
    assertThat(url.toString(), containsString("c=123 456 789"));
  }

  @org.junit.Test
  public void testUrlParseWithModifiedAndEncodedParams() throws Exception {
    ModifiableURL url = ModifiableURL.parse("http://www.google.com?a=123&b=456&c=123,456,789");
    url.addParam("d", "123 456 789");
    assertThat(url.getUrlParams().size(), equalTo(4));
    assertThat(url.toString(), containsString("d=123+456+789"));

    url.addParam("e", "test");
    assertThat(url.getUrlParams(), hasEntry("e","test"));
  }

  @org.junit.Test
  public void testUrlParseWithReplacementParams() throws Exception {
    String WX_URL = "http://wx.e.com/weather.php?postalCode=_POSTAL_CODE_&default=_DEFAULT_POSTAL_CODE_";
    ModifiableURL url = ModifiableURL.parse(WX_URL);
    url.replaceFirstMatchingValue("_POSTAL_CODE_", "55901");
    assertThat(url.getUrlParams().size(), equalTo(2));
    assertThat(url.toString(), containsString("postalCode=55901"));
    assertThat(url.toString(), containsString("default=_DEFAULT_POSTAL_CODE_"));

    url = ModifiableURL.parse(WX_URL);
    url.replaceFirstMatchingValue("_POSTAL_CODE_", "55901 12345");
    assertThat(url.getUrlParams().size(), equalTo(2));
    assertThat(url.toString(), containsString("postalCode=55901+12345"));
    assertThat(url.toString(), containsString("default=_DEFAULT_POSTAL_CODE_"));
  }

  public void testTryParse() throws Exception {
    String WX_URL = "http://wx.e.com/weather.php?postalCode=_POSTAL_CODE_&default=_DEFAULT_POSTAL_CODE_";
    Optional<ModifiableURL> url = ModifiableURL.tryParse(WX_URL);
    MatcherAssert.assertThat(url.isPresent(), equalTo(true));

    url = ModifiableURL.tryParse("");
    MatcherAssert.assertThat(url.isPresent(), equalTo(false));
  }
}
