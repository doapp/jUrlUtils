package com.doapps.utils.url;

import com.google.common.base.Optional;

import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

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
  public void testUrlParseWithFragmentModifiedAndEncodedParams() throws Exception {
    ModifiableURL url = ModifiableURL.parse("http://www.google.com?a=123&b=456&c=123,456,789#test");
    url.addParam("d", "123 456 789");
    assertThat(url.getUrlParams().size(), equalTo(4));
    assertThat(url.toString(), containsString("d=123+456+789"));

    url.addParam("e", "test");
    assertThat(url.getUrlParams(), hasEntry("e", "test"));
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

  @org.junit.Test
  public void testTryParse() throws Exception {
    String WX_URL = "http://wx.e.com/weather.php?postalCode=_POSTAL_CODE_&default=_DEFAULT_POSTAL_CODE_";
    Optional<ModifiableURL> url = ModifiableURL.tryParse(WX_URL);
    MatcherAssert.assertThat(url.isPresent(), equalTo(true));

    url = ModifiableURL.tryParse("");
    MatcherAssert.assertThat(url.isPresent(), equalTo(false));
  }

  @org.junit.Test
  public void testTryKVUEParse() throws Exception {
    String WX_URL = "http://www.gannett-cdn.com/-mm-/239ec2f186d0d97cfd2e6532b75efa2676f3a6c3/ttl=120&r=x513&c"
                    + "=680x510/http/archive.kvue.com/weather/images/core/HOURBYHOUR_IPAD.jpg";
    Optional<ModifiableURL> url = ModifiableURL.tryParse(WX_URL);
    MatcherAssert.assertThat(url.isPresent(), equalTo(true));
    MatcherAssert.assertThat(url.get().getUrlParams().size(), equalTo(0));
    MatcherAssert.assertThat(url.get().toString(), equalTo(WX_URL));
  }

  @org.junit.Test
  public void testTryKVUEModParse() throws Exception {
    String WX_URL = "http://www.gannett-cdn.com/-mm-/239ec2f186d0d97cfd2e6532b75efa2676f3a6c3/ttl=120&r=x513&c"
                    + "=680x510/http/archive.kvue.com/weather/images/core/HOURBYHOUR_IPAD.jpg";
    Optional<ModifiableURL> url = ModifiableURL.tryParse(WX_URL);
    MatcherAssert.assertThat(url.isPresent(), equalTo(true));
    url.get().addParam("r", "12300");
    MatcherAssert.assertThat(url.get().getUrlParams().size(), equalTo(1));
    MatcherAssert.assertThat(url.get().toString(), startsWith(WX_URL));
  }

  private String [] URLS = {
      "http://user:password@example.com:8080/path?query=value#fragment",
      "http://user:password@example.com:8080",
      "http://user:password@example.com:8080",
      "http://user:password@example.com:8080/path?query=value#fragment",
      "http://example.com/",
      "http://example.com/path",
      "http://www.ietf.org/rfc/rfc2396.txt",
      "http://192.0.2.16:8000",
      "http://EXAMPLE.COM/",
      "http://user%40123%21@example.com/",
      "http://:newpass@example.com",
      "http://:%23secret%40123%21@example.com/",
      "http://[fe80:0:0:0:200:f8ff:fe21:67cf]/",
      "http://example.com/%7Esmith/",
      "http://example.com/%E8",
      "http://example.com/path%2Fsegment/",
      "http://example.com/?q=string",
      "http://example.com:%38%30/",
      "http://example.com/../..",
      "http://example.com/file.txt;parameter",
      "http://user:pass@example.com/path/to/resource?query=x",
      "http://user:pass@example.com/path/to/resource?query=x#fragment",
      "https://newuser:newpass@example.com:443",
      "http://newuser:newpass@example.com:80",
      "http://newuser:newpass@example.com",
      "http://user:pass@example.com/newpath/to/resource?query=x#fragment",
      "http://user:pass@example.com?query=x#fragment",
      "http://user:pass@example.com/path/to/resource?newquery=x#fragment",
      "http://user:pass@example.com/path/to/resource#fragment",
      "http://user:pass@example.com/path/to/resource?query=x#newfragment",
      "http://user:pass@example.com/path/to/resource?query=x",
      "http://user:pass@example.com/path/to/resource?query=x#newfragment",
      "http://user:pass@example.com/path/to/resource?query=x",
      "http://newuser:newpass@example.com/path/to/resource?query=x#fragment",
      "http://example.com/path/to/resource?query=x#fragment",
      "http://user:pass@example.com/newpath?query=x#fragment",
      "http://foo:bar@baz:42/path/to/resource?query=x#fragment",
      "http://user:pass@example.com/path/to/resource?query=x#fragment",
      "http://foo:bar@baz:42/path/to/resource?query=x#fragment"
      };

  private static String BAD_URL_BASE = "http://example.com/file";

  private static String [] BAD_URLS = {
      "http://example.com/file?",
      "http://example.com/file?val",
      "http://example.com/file?val=",
      "http://user:pass@example.com:42/newpath?#fragment",
  };

  private static String REVISABLE_URL_BASE = "http://example.com/file?key=value";

  private static String [] REVISABLE_URLS = {
      "http://example.com/file?key=value&otherkey=",
      "http://example.com/file?key=value&otherkey"
  };


  @org.junit.Test
  public void testTryUserPasswordParse() throws Exception {
    for(String URL : URLS) {
      Optional<ModifiableURL> url = ModifiableURL.tryParse(URL);
      MatcherAssert.assertThat(URL, url.isPresent(), equalTo(true));
    }
  }

  @org.junit.Test
  public void testTryBadUrls() throws Exception {
    for (String testUrl : BAD_URLS) {
      Optional<ModifiableURL> urlOptional = ModifiableURL.tryParse(testUrl);
      assertThat("On url " + testUrl, urlOptional.isPresent(), is(false));
    }
  }

  @Test
  public void testRevisableUrls() throws Exception {
    for (String testUrl : REVISABLE_URLS) {
      Optional<ModifiableURL> urlOptional = ModifiableURL.tryParse(testUrl);
      assertThat("On url " + testUrl, urlOptional.isPresent(), is(true));
      assertThat("On url " + testUrl, urlOptional.get().toString(), is(REVISABLE_URL_BASE));
    }
  }
}
