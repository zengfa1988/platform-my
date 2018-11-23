package com.zengfa.platform.data.hibernate;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 封装Hibernate原生API的DAO泛型基类.
 * 
 * 可在Service层直接使用,也可以扩展泛型DAO子类使用.
 * 参考Spring2.5自带的Petlinc例子,取消了HibernateTemplate,直接使用Hibernate原生API.
 * 
 * @param <T>
 *            DAO操作的对象类型
 * @param <PK>
 *            主键类型
 * @author lichangwen
 * 
 */
@SuppressWarnings("unchecked")
public class SuperHibernateDao<T, PK extends Serializable> {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected SessionFactory sessionFactory;

	protected Class<T> entityClass;

	/**
	 * 用于Dao层子类使用的构造函数. 通过子类的泛型定义取得对象类型Class. eg. public class UserDao extends
	 * SimpleHibernateDao<User, Long>
	 */
	public SuperHibernateDao() {
		this.entityClass = getSuperClassGenricType(getClass(),0);
	}
	
	@SuppressWarnings("rawtypes")
	public static Class getSuperClassGenricType(final Class clazz, final int index) {
		Type genType = clazz.getGenericSuperclass();
		if (!(genType instanceof ParameterizedType)) {
			return Object.class;
		}
		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
		if (index >= params.length || index < 0) {;
			return Object.class;
		}
		if (!(params[index] instanceof Class)) {
			return Object.class;
		}
		return (Class) params[index];
	}

	/**
	 * 用于用于省略Dao层, 在Service层直接使用通用SimpleHibernateDao的构造函数. 在构造函数中定义对象类型Class.
	 * eg. SimpleHibernateDao<User, Long> userDao = new SimpleHibernateDao<User,
	 * Long>(sessionFactory, User.class);
	 * 
	 * @param sessionFactory
	 * @param entityClass
	 */
	public SuperHibernateDao(@Qualifier("sessionFactory") final SessionFactory sessionFactory, final Class<T> entityClass) {
		this.sessionFactory = sessionFactory;
		this.entityClass = entityClass;
	}

	/**
	 * 取得sessionFactory.
	 * 
	 * @return SessionFactory
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * 采用@Autowired按类型注入SessionFactory,当有多个SesionFactory的时候Override本函数.
	 * 
	 * @param sessionFactory
	 */
	@Autowired
	public void setSessionFactory(@Qualifier("sessionFactory") final SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * 取得当前Session.
	 * 
	 * @return Session
	 */
	public Session getSession() {
		return sessionFactory.getCurrentSession();
	}

	/**
	 * 保存新增或修改的对象.
	 * 
	 * @param entity
	 * 
	 */
	public void save(final Object entity) {
		getSession().saveOrUpdate(entity);
		logger.debug("save or update entity: {}", entity);
	}
	
	/**
	 * 更新对象
	 * @param entity
	 */
	public void update(final Object entity) {
		getSession().update(entity);
		logger.debug("save or update entity: {}", entity);
	}
	

	/**
	 * 删除对象.
	 * 
	 * @param entity
	 *            对象必须是session中的对象或含id属性的transient对象.
	 */
	public void delete(final Object entity) {
		getSession().delete(entity);
		logger.debug("delete entity: {}", entity);
	}

	/**
	 * 按id删除对象.
	 */
	public void delete(final PK id) {
		delete(get(id));
		logger.debug("delete entity {},id is {}", entityClass.getSimpleName(), id);
	}

	/**
	 * 按id获取对象.
	 */
	public T get(final PK id) {
		return (T) getSession().get(entityClass, id);
	}

	/**
	 * 获取全部对象.
	 */
	public List<T> getAll() {
		return find();
	}

	/**
	 * 按id列表获取对象.
	 */
	public List<T> findByIds(List<PK> ids) {
		return find(Restrictions.in(getIdName(), ids));
	}
	
	/**
	 * 按HQL查询对象列表.
	 * 
	 * @param values
	 *            数量可变的参数,按顺序绑定.
	 */
	public <X> List<X> find(final String hql, final List<Object> values) {
		return createQuery(hql, values.toArray()).list();
	}

	/**
	 * 按HQL查询对象列表.
	 * 
	 * @param values
	 *            数量可变的参数,按顺序绑定.
	 */
	public <X> List<X> find(final String hql, final Object... values) {
		return createQuery(hql, values).list();
	}
	
	public <X> List<X> findTop(final String hql, final int topSize,final Object... values) {
		Query q = createQuery(hql, values);
		q.setFirstResult(0);
		q.setMaxResults(topSize);
		return q.list();
	}

	/**
	 * 按HQL查询对象列表.
	 * 
	 * @param values
	 *            命名参数,按名称绑定.
	 */
	public <X> List<X> find(final String hql, final Map<String, Object> values) {
		return createQuery(hql, values).list();
	}

	/**
	 * 按HQL查询唯一对象.
	 * 
	 * @param values
	 *            数量可变的参数,按顺序绑定.
	 */
	public <X> X findUnique(final String hql, final Object... values) {
		return (X) createQuery(hql, values).uniqueResult();
	}

	/**
	 * 按HQL查询唯一对象.
	 * 
	 * @param values
	 *            命名参数,按名称绑定.
	 */
	public <X> X findUnique(final String hql, final Map<String, Object> values) {
		return (X) createQuery(hql, values).uniqueResult();
	}

	/**
	 * 执行HQL进行批量修改/删除操作.
	 */
	public int batchExecute(final String hql, final Object... values) {
		return createQuery(hql, values).executeUpdate();
	}

	/**
	 * 执行HQL进行批量修改/删除操作.
	 * 
	 * @return 更新记录数.
	 */
	public int batchExecute(final String hql, final Map<String, Object> values) {
		return createQuery(hql, values).executeUpdate();
	}

	/**
	 * 根据查询HQL与参数列表创建Query对象.
	 * 
	 * 本类封装的find()函数全部默认返回对象类型为T,当不为T时使用本函数.
	 * 
	 * @param values
	 *            数量可变的参数,按顺序绑定.
	 */
	public Query createQuery(final String queryString, final Object... values) {
		Query query = getSession().createQuery(queryString);
		if (ArrayUtils.isNotEmpty(values)) {
			for (int i = 0; i < values.length; i++) {
				query.setParameter(i, values[i]);
			}
		}
		return query;
	}

	/**
	 * 根据查询HQL与参数列表创建Query对象.
	 * 
	 * @param values
	 *            命名参数,按名称绑定.
	 */
	public Query createQuery(final String queryString, final Map<String, Object> values) {
		Query query = getSession().createQuery(queryString);
		if (values != null) {
			query.setProperties(values);
		}
		return query;
	}

	/**
	 * 按Criteria查询对象列表.
	 * 
	 * @param criterions
	 *            数量可变的Criterion.
	 */
	public List<T> find(final Criterion... criterions) {
		return createCriteria(criterions).list();
	}

	/**
	 * 按Criteria查询唯一对象.
	 * 
	 * @param criterions
	 *            数量可变的Criterion.
	 */
	public T findUnique(final Criterion... criterions) {
		return (T) createCriteria(criterions).uniqueResult();
	}

	/**
	 * 根据Criterion条件创建Criteria.
	 * 
	 * 本类封装的find()函数全部默认返回对象类型为T,当不为T时使用本函数.
	 * 
	 * @param criterions
	 *            数量可变的Criterion.
	 */
	public Criteria createCriteria(final Criterion... criterions) {
		Criteria criteria = getSession().createCriteria(entityClass);
		for (Criterion c : criterions) {
			criteria.add(c);
		}
		return criteria;
	}
	
	/**
	 * @param entityClass
	 * @return
	 */
	public Criteria createCriteria(final Class<?> entityCls) {
		return getSession().createCriteria(entityCls);
	}
	
	/**
	 * @return
	 */
	public Criteria createCriteria() {
		return getSession().createCriteria(entityClass);
	}

	/**
	 * 取得对象的主键名.
	 */
	public String getIdName() {
		ClassMetadata meta = getSessionFactory().getClassMetadata(entityClass);
		return meta.getIdentifierPropertyName();
	}
}