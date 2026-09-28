package studyweb.cus.constant.admin;

import java.util.List;

public final class WebsiteManagementConstants {

  private WebsiteManagementConstants() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static final String PREFIX_HTTP = "http://";
  public static final String PREFIX_HTTPS = "https://";
  public static final String PREFIX_MAILTO = "mailto:";
  public static final String PREFIX_TEL = "tel:";
  public static final String PREFIX_PROTOCOL_RELATIVE = "//";
  public static final String PREFIX_WWW = "www.";

  public static final List<String> EXTERNAL_URL_PREFIXES =
      List.of(PREFIX_HTTP, PREFIX_HTTPS, PREFIX_MAILTO, PREFIX_TEL, PREFIX_PROTOCOL_RELATIVE);

  public static final int MAX_FOOTER_LINKS_PER_CATEGORY = 8;
}
