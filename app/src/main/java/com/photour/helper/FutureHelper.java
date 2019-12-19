package com.photour.helper;

import com.photour.database.AppDatabase;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * A helper class for {@link Callable} and {@link Future}
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class FutureHelper {

  /**
   * Check whether @Insert, @Update or @Delete task has completed successfully
   *
   * @param task The task to be executed asynchronously
   * @return boolean {@code true} if the task has completed successfully, {@code false} otherwise
   */
  public static boolean rowOperationFuture(Callable<Integer> task) {
    Future<Integer> future = AppDatabase.databaseExecutor.submit(task);

    try {
      return future.get() == 1;
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
    }

    return false;
  }

  /**
   * Retrieve data of generic type from database
   *
   * @param task The task to be executed asynchronously
   * @param <T> the result type of method {@code call}
   * @return T the computed result
   */
  public static <T> T genericFuture(Callable<T> task) {
    Future<T> future = AppDatabase.databaseExecutor.submit(task);

    try {
      return future.get();
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Retrieve data of type {@link String} from database
   *
   * @param task The task to be executed asynchronously
   * @return String the computed result
   */
  public static String stringFuture(Callable<String> task) {
    Future<String> future = AppDatabase.databaseExecutor.submit(task);

    try {
      return future.get();
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
    }

    return "";
  }
}
