package org.avaje.metric;

import java.lang.reflect.Method;

import javax.management.ObjectName;

/**
 * Provides a name for metrics.
 * <p>
 * Typically names are based on a class and method name.
 * </p>
 */
public class MetricName implements Comparable<MetricName> {

  private final String group;
  private final String type;
  private final String name;
  private final String scope;
  private final String simpleName;
  private final String mBeanName;
  private final ObjectName mBeanObjectName;

  /**
   * Creates a new {@link MetricName} without a scope.
   * 
   * @param klass
   *          the {@link Class} to which the {@link TimedMetric} belongs
   * @param name
   *          the name of the {@link TimedMetric}
   */
  public MetricName(Class<?> klass, String name) {
    this(klass, name, null);
  }

  /**
   * Creates a new {@link MetricName} without a scope.
   * 
   * @param group
   *          the group to which the {@link TimedMetric} belongs
   * @param type
   *          the type to which the {@link TimedMetric} belongs
   * @param name
   *          the name of the {@link TimedMetric}
   */
  public MetricName(String group, String type, String name) {
    this(group, type, name, null);
  }

  /**
   * Creates a new {@link MetricName} with just a group and name.
   * 
   * @param group
   *          the group to which the {@link TimedMetric} belongs
   * @param name
   *          the name of the {@link TimedMetric}
   */
  public MetricName(String group, String name) {
    this(group, null, name, null);
  }
  
  /**
   * Creates a new {@link MetricName} without a scope.
   * 
   * @param klass
   *          the {@link Class} to which the {@link TimedMetric} belongs
   * @param name
   *          the name of the {@link TimedMetric}
   * @param scope
   *          the scope of the {@link TimedMetric}
   */
  public MetricName(Class<?> klass, String name, String scope) {
    this(klass.getPackage() == null ? "" : klass.getPackage().getName(), klass.getSimpleName()
        .replaceAll("\\$$", ""), name, scope);
  }

  /**
   * Creates a new {@link MetricName} without a scope.
   * 
   * @param group
   *          the group to which the {@link TimedMetric} belongs
   * @param type
   *          the type to which the {@link TimedMetric} belongs
   * @param name
   *          the name of the {@link TimedMetric}
   * @param scope
   *          the scope of the {@link TimedMetric}
   */
  public MetricName(String group, String type, String name, String scope) {
    this(group, type, name, scope, createMBeanName(group, type, name, scope));
  }

  /**
   * Creates a new {@link MetricName} without a scope.
   * 
   * @param group
   *          the group to which the {@link TimedMetric} belongs
   * @param type
   *          the type to which the {@link TimedMetric} belongs
   * @param name
   *          the name of the {@link TimedMetric}
   * @param scope
   *          the scope of the {@link TimedMetric}
   * @param mBeanName
   *          the 'ObjectName', represented as a string, to use when registering
   *          the MBean.
   */
  public MetricName(String group, String type, String name, String scope, String mBeanName) {
    if (group == null) {
      throw new IllegalArgumentException("group needs to be specified");
    }
    this.group = group;
    this.type = type;
    this.name = name;
    this.scope = scope;
    this.mBeanName = mBeanName;
    this.mBeanObjectName = createObjectName(mBeanName);
    this.simpleName = createSimpleName(group, type, name);
  }

  /**
   * Create a MetricName with the nameSuffix appended to the original name.
   * <p>
   * Used to create an 'error' mbean name.
   * </p>
   */
  public MetricName deriveWithNameSuffix(String nameSuffix) {
    return new MetricName(group, type, name + nameSuffix, scope);
  }

  /**
   * Create a similar MetricName changing just the name.
   * <p>
   * Typically used via MetricNameCache.
   * </p>
   */
  public MetricName deriveWithName(String newName) {
    return new MetricName(group, type, newName, scope);
  }

  private ObjectName createObjectName(String name) {
    try {
      return new ObjectName(mBeanName);
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Returns the group to which the {@link TimedMetric} belongs. For class-based
   * metrics, this will be the package name of the {@link Class} to which the
   * {@link TimedMetric} belongs.
   * 
   * @return the group to which the {@link TimedMetric} belongs
   */
  public String getGroup() {
    return group;
  }

  /**
   * Returns the type to which the {@link TimedMetric} belongs. For class-based
   * metrics, this will be the simple class name of the {@link Class} to which
   * the {@link TimedMetric} belongs.
   * 
   * @return the type to which the {@link TimedMetric} belongs
   */
  public String getType() {
    return type;
  }

  /**
   * Returns the name of the {@link TimedMetric}.
   * 
   * @return the name of the {@link TimedMetric}
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the scope of the {@link TimedMetric}.
   * 
   * @return the scope of the {@link TimedMetric}
   */
  public String getScope() {
    return scope;
  }

  /**
   * Returns {@code true} if the {@link TimedMetric} has a scope, {@code false}
   * otherwise.
   * 
   * @return {@code true} if the {@link TimedMetric} has a scope
   */
  public boolean hasScope() {
    return scope != null;
  }

  /**
   * Return a simple java like name.
   */
  public String getSimpleName() {
    return simpleName;
  }

  /**
   * Returns the MBean name for the {@link TimedMetric} identified by this
   * metric name.
   * 
   * @return the MBean name
   */
  public String getMBeanName() {
    return mBeanName;
  }

  public ObjectName getMBeanObjectName() {
    return mBeanObjectName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final MetricName that = (MetricName) o;
    return mBeanName.equals(that.mBeanName);
  }

  @Override
  public int hashCode() {
    return mBeanName.hashCode();
  }

  @Override
  public String toString() {
    return simpleName;
  }

  @Override
  public int compareTo(MetricName o) {
    return mBeanName.compareTo(o.mBeanName);
  }

  private static String createMBeanName(String group, String type, String name, String scope) {
    StringBuilder sb = new StringBuilder(80);
    sb.append(group);
    if (type != null) {
      sb.append(":type=").append(type);
    }
    if (scope != null) {
      sb.append(",scope=").append(scope);
    }
    if (name != null && name.length() > 0) {
      sb.append(",name=").append(name);
    }
    return sb.toString();
  }

  private static String createSimpleName(String group, String type, String name) {
    StringBuilder sb = new StringBuilder(80);
    sb.append(group);
    if (type != null) {
      sb.append(".").append(type);
    }
    if (name != null && name.length() > 0) {
      sb.append(".").append(name);
    }
    return sb.toString().replace(' ', '-');
  }

  /**
   * If the group is empty, use the package name of the given class. Otherwise
   * use group
   * 
   * @param group
   *          The group to use by default
   * @param klass
   *          The class being tracked
   * @return a group for the metric
   */
  public static String chooseGroup(String group, Class<?> klass) {
    if (group == null || group.isEmpty()) {
      group = klass.getPackage() == null ? "" : klass.getPackage().getName();
    }
    return group;
  }

  /**
   * If the type is empty, use the simple name of the given class. Otherwise use
   * type
   * 
   * @param type
   *          The type to use by default
   * @param klass
   *          The class being tracked
   * @return a type for the metric
   */
  public static String chooseType(String type, Class<?> klass) {
    if (type == null || type.isEmpty()) {
      type = klass.getSimpleName().replaceAll("\\$$", "");
    }
    return type;
  }

  /**
   * If name is empty, use the name of the given method. Otherwise use name
   * 
   * @param name
   *          The name to use by default
   * @param method
   *          The method being tracked
   * @return a name for the metric
   */
  public static String chooseName(String name, Method method) {
    if (name == null || name.isEmpty()) {
      name = method.getName();
    }
    return name;
  }
}
