package com.mg.uav.tools;


import android.content.Context;

import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;


/**
 * @author chuangzhang
 */
public class MapConvertUtils {

  /**
   * 圆周率
   */
  private static double PI = Math.PI;
  /**
   * 轴
   */
  private static double AXIS = 6378245.0;

  /**
   * 偏移量 (a^2 - b^2) / a^2
   */
  private static double OFFSET = 0.00669342162296594323;


  /**
   * 坐标转换-转为高德坐标系(高德API)
   *
   * @param lat
   * @param lng
   * @return
   */
  public static LatLng getGDLatLng(double lat, double lng, Context context) {

//    CoordinateConverter converter = new CoordinateConverter(context);
    CoordinateConverter converter = new CoordinateConverter(context);
    // CoordType.GPS 待转换坐标类型
    converter.from(CoordinateConverter.CoordType.GPS);
    // sourceLatLng待转换坐标点 DPoint类型
    converter.coord(new LatLng(lat, lng));
    // 执行转换操作
    LatLng desLatLng = converter.convert();
    return new LatLng(desLatLng.latitude, desLatLng.longitude);
  }

  /**
   * WGS84=》GCJ02   地球坐标系=>火星坐标系
   *
   * @param wgLat
   * @param wgLon
   * @return
   */
  private static double[] wgs2GCJ(double wgLat, double wgLon) {
    double[] latlon = new double[2];
    double[] deltaD = delta(wgLat, wgLon);
    latlon[0] = wgLat + deltaD[0];
    latlon[1] = wgLon + deltaD[1];
    return latlon;
  }


  /**
   * 坐标转换-转为大疆坐标系 GCJ02=>WGS84   火星坐标系=>地球坐标系（精确）
   *
   * @param gcjLat
   * @param gcjLon
   * @return
   */
  public static double[] getDJILatLng(double gcjLat, double gcjLon) {
    double initDelta = 0.01;
    double threshold = 0.000000001;
    double dLat = initDelta, dLon = initDelta;
    double mLat = gcjLat - dLat, mLon = gcjLon - dLon;
    double pLat = gcjLat + dLat, pLon = gcjLon + dLon;
    double wgsLat, wgsLon, i = 0;
    while (true) {
      wgsLat = (mLat + pLat) / 2;
      wgsLon = (mLon + pLon) / 2;
      double[] tmp = wgs2GCJ(wgsLat, wgsLon);
      dLat = tmp[0] - gcjLat;
      dLon = tmp[1] - gcjLon;
      if ((Math.abs(dLat) < threshold) && (Math.abs(dLon) < threshold)) {
        break;
      }

      if (dLat > 0) {
        pLat = wgsLat;
      } else {
        mLat = wgsLat;
      }
      if (dLon > 0) {
        pLon = wgsLon;
      } else {
        mLon = wgsLon;
      }

      if (++i > 10000) {
        break;
      }
    }
    double[] result={wgsLat,wgsLon};
    return result;
    //return new LatLng(wgsLat, wgsLon);
  }


  /**
   * 转换函数
   *
   * @param wgLat
   * @param wgLon
   * @return
   */
  private static double[] delta(double wgLat, double wgLon) {
    double[] latlng = new double[2];
    double dLat = transformLat(wgLon - 105.0, wgLat - 35.0);
    double dLon = transformLon(wgLon - 105.0, wgLat - 35.0);
    double radLat = wgLat / 180.0 * PI;
    double magic = Math.sin(radLat);
    magic = 1 - OFFSET * magic * magic;
    double sqrtMagic = Math.sqrt(magic);
    dLat = (dLat * 180.0) / ((AXIS * (1 - OFFSET)) / (magic * sqrtMagic) * PI);
    dLon = (dLon * 180.0) / (AXIS / sqrtMagic * Math.cos(radLat) * PI);
    latlng[0] = dLat;
    latlng[1] = dLon;
    return latlng;
  }

  /**
   * 是否超出国界
   *
   * @param lat
   * @param lon
   * @return
   */
  private static boolean outOfChina(double lat, double lon) {
    if (lon < 72.004 || lon > 137.8347) {
      return true;
    }
    if (lat < 0.8293 || lat > 55.8271) {
      return true;
    }
    return false;
  }


  /**
   * 转换纬度
   *
   * @param x
   * @param y
   * @return
   */
  private static double transformLat(double x, double y) {
    double ret =
        -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
    ret = getRet(x, y, ret);
    ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
    return ret;
  }

  private static double getRet(double x, double y, double ret) {
    ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
    ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
    return ret;
  }

  /**
   * 转换经度
   *
   * @param x
   * @param y
   * @return
   */
  private static double transformLon(double x, double y) {
    double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
    ret = getRet(x, x, ret);
    ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
    return ret;
  }


  public static void main(String[] args) {
    double[] result =getDJILatLng(31.306237, 120.665986);
    System.out.println(result[0]+"   "+result[1]);
  }

  /**
   * 给定一个圆心的经纬度和半径，求圆弧上的坐标
   * @param centerPoint 经纬度坐标
   * @param radius 半径
   */
  public static List<String> getCircleAxis(Double[] centerPoint, int radius) {
    Double X; //经度
    Double Y; //纬度
    double r = 6371000.79;
    List<String> optionsAxis = new ArrayList<>();
    int numpoints = 360; // 角度
    double phase = 2 * Math.PI / numpoints;

    //画图
    for (int i = 0; i < numpoints; i++) {
      /**
       * 计算坐标点
       */
      double dx = (radius * Math.cos(i * phase));
      double dy = (radius * Math.sin(i * phase)); //乘以1.6 椭圆比例

      /**
       * 转换成经纬度
       */
      double dlng = dx / (r * Math.cos(centerPoint[1] * Math.PI / 180) * Math.PI / 180); //纬度的差值
      double dlat = dy / (r * Math.PI / 180);  // 经度的差值

      X = centerPoint[1] + dlat;
      Y = centerPoint[0] + dlng;
      String optAxis = Y+","+X;
      optionsAxis.add(optAxis);
    }
    return optionsAxis;
  }

}

