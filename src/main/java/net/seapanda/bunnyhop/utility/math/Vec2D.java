/*
 * Copyright 2017 K.Koike
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.seapanda.bunnyhop.utility.math;

import java.io.Serializable;

/**
 * 二次元ベクトル.
 *
 * @author K.Koike
 */
public class Vec2D implements Serializable {

  public double x;
  public double y;

  public Vec2D() {
    this.x = 0;
    this.y = 0;
  }

  public Vec2D(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public Vec2D(Vec2D org) {
    this.x = org.x;
    this.y = org.y;
  }

  /**
   * 引数で指定した数の方が現在の値より大きい場合, その数で置き換える.
   * 片方の要素だけ大きい場合は、その要素だけ置き換える.
   */
  public Vec2D updateIfGreater(double x, double y) {
    this.x = Math.max(this.x, x);
    this.y = Math.max(this.y, y);
    return this;
  }

  /**
   * 引数で指定した数の方が現在の値より大きい場合, その数で置き換える.
   * 片方の要素だけ大きい場合は、その要素だけ置き換える.
   */
  public Vec2D updateIfGreter(Vec2D v) {
    return updateIfGreater(v.x, v.y);
  }

  /**
   * 引数で指定した数の方が現在の値のより小さい場合, その数で置き換える.
   * 片方の要素だけ小さい場合は、その要素だけ置き換える.
   */
  public Vec2D updateIfLess(double x, double y) {
    this.x = Math.min(this.x, x);
    this.y = Math.min(this.y, y);
    return this;
  }

  /**
   * 引数で指定した数の方が現在の値より小さい場合, その数で置き換える.
   * 片方の要素だけ小さい場合は、その要素だけ置き換える.
   */
  public Vec2D updateIfLess(Vec2D v) {
    return updateIfLess(v.x, v.y);
  }

  /** 引数で指定した要素を現在の値に足す. */
  public Vec2D add(double x, double y) {
    this.x += x;
    this.y += y;
    return this;
  }

  /** 引数で指定した要素を現在の値に足す. */
  public Vec2D add(Vec2D v) {
    return add(v.x, v.y);
  }

  /** 引数で指定した値を現在の値から引く. */
  public Vec2D sub(double x, double y) {
    this.x -= x;
    this.y -= y;
    return this;
  }

  /** 引数で指定した値を現在の値から引く. */
  public Vec2D sub(Vec2D v) {
    return sub(v.x, v.y);
  }

  /** 引数で指定した値をこのオブジェクトに設定する. */
  public Vec2D set(double x, double y) {
    this.x = x;
    this.y = y;
    return this;
  }

  /** 引数で指定した値をこのオブジェクトに設定する. */
  public Vec2D set(Vec2D v) {
    return set(v.x, v.y);
  }

  /** 引数で指定した要素を現在の値に掛ける. */
  public Vec2D mul(double v) {
    this.x *= v;
    this.y *= v;
    return this;
  }

  /** このベクトルの長さを求める. */
  public double len() {
    return Math.sqrt(len2());
  }
  
  /** このベクトルの長さを二乗した値を求める. */
  public double len2() {
    return this.x * this.x + this.y * this.y;
  }

  /** 引数で指定したベクトルとの内積を求める. */
  public double dot(double x, double y) {
    return this.x * x + this.y * y;
  }

  /** 引数で指定したベクトルとの内積を求める. */
  public double dot(Vec2D v) {
    return dot(v.x, v.y);
  }

  @Override
  public boolean equals(Object point) {
    if (point instanceof Vec2D) {
      return (x == ((Vec2D) point).x) && (y == ((Vec2D) point).y);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    int hash = Long.hashCode(Double.doubleToLongBits(this.x));
    hash = 67 * hash + Long.hashCode(Double.doubleToLongBits(this.y));
    return hash;
  }

  @Override
  public String toString() {
    return "x: %s,  y: %s".formatted(x, y);
  }
}
