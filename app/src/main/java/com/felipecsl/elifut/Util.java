package com.felipecsl.elifut;

import com.felipecsl.elifut.services.ResponseBodyMapper;
import com.felipecsl.elifut.services.ResponseMapper;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import retrofit.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public final class Util {
  private Util() {
  }

  public static void closeQuietly(Closeable closeable) {
    try {
      Closeables.close(closeable, true);
    } catch (IOException ignored) {
    }
  }

  public static <T> Class<? extends T> autoValueTypeFor(Class<T> type) {
    try {
      String name = type.getName();
      String packageName = name.substring(0, name.lastIndexOf('.'));
      //noinspection unchecked
      return (Class<? extends T>) Class.forName(packageName + ".AutoValue_" + type.getSimpleName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> List<T> listSupertype(List<? extends T> list) {
    return Lists.transform(list, i -> (T) i);
  }

  public static <T> Observable.Transformer<Response<T>, T> apiObservableTransformer() {
    return (Observable<Response<T>> observable) ->
        observable.subscribeOn(Schedulers.io())
            .flatMap(ResponseMapper.<T>instance())
            .map(ResponseBodyMapper.<T>instance())
            .observeOn(AndroidSchedulers.mainThread());
  }
}
