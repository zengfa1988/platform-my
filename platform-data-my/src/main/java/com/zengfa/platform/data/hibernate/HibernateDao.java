package com.zengfa.platform.data.hibernate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.transform.Transformers;
import org.hibernate.type.BigDecimalType;
import org.hibernate.type.CharacterType;
import org.hibernate.type.DateType;
import org.hibernate.type.DoubleType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import com.zengfa.platform.util.bean.Page;
import com.zengfa.platform.util.bean.PageBean;
import com.zengfa.platform.util.bean.Pagination;


/**
 * 封装带分页功能的Hibernat泛型基类.
 * 
 * 扩展功能包括分页查询,按属性过滤条件列表查询. 可在Service层直接使用,也可以扩展泛型DAO子类使用,见两个构造函数的注释.
 * 
 * @param <T>
 *            DAO操作的对象类型
 * @param <PK>
 *            主键类型
 * 
 * @author lichangwen
 */
public class HibernateDao<T, PK extends Serializable> extends SuperHibernateDao<T, PK> {
	// 批量处理数量
	private static int BATCH_SIZE = 50;

	/**
	 * 用于Dao层子类使用的构造函数. 通过子类的泛型定义取得对象类型Class. eg. public class UserDao extends
	 * HibernateDao<User, Long>{ }
	 */
	public HibernateDao() {
		super();
	}

	/**
	 * 用于省略Dao层, Service层直接使用通用HibernateDao的构造函数. 在构造函数中定义对象类型Class. eg.
	 * HibernateDao<User, Long> userDao = new HibernateDao<User,
	 * Long>(sessionFactory, User.class);
	 */
	public HibernateDao(final SessionFactory sessionFactory, final Class<T> entityClass) {
		super(sessionFactory, entityClass);
	}

	/**
	 * 按HQL分页查询.
	 * 
	 * @param page
	 *            分页参数.不支持其中的orderBy参数.
	 * @param hql
	 *            hql语句.
	 * @param values
	 *            数量可变的查询参数,按顺序绑定.
	 * 
	 * @return 分页查询结果, 附带结果列表及所有查询时的参数.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Page<T> findPage(final Page<T> page, final String hql, final Object... values) {
		Query q = createQuery(hql, values);
		if (page.isAutoCount()) {
			long totalCount = countHqlResult(hql, values);
			page.setTotalCount(totalCount);
		}
		setPageParameter(q, page);
		List result = q.list();
		page.setData(result);
		return page;
	}

	/**
	 * @param page
	 * @param hql
	 * @param values
	 * @return
	 */
	public Pagination findPagination(final Page<T> page, final String hql, final Object... values) {
		Query q = createQuery(hql, values);
		long totalCount = countHqlResult(hql, values);
		page.setTotalCount(totalCount);
		setPageParameter(q, page);
		return Pagination.createPagination(totalCount, q.list());
	}

	public Pagination findPaginationForEntity(final Page<?> page, final String hql, final Object... values) {
		Query q = createQuery(hql, values);
		long totalCount = countHqlResult(hql, values);
		page.setTotalCount(totalCount);
		q.setFirstResult(page.getFirst() - 1);
		q.setMaxResults(page.getPageSize());
		return Pagination.createPagination(totalCount, q.list());
	}

	public Pagination findPagination(final Page<?> page, final Criteria criteria) {
		Pagination pagination = new Pagination();
		criteria.setProjection(Projections.rowCount());
		Long total = criteria.uniqueResult() == null ? 0 : (Long) criteria.uniqueResult();
		pagination.setTotal(total);
		criteria.setProjection(null);
		criteria.setFirstResult(page.getFirst() - 1);
		criteria.setMaxResults(page.getPageSize());
		pagination.setRows(criteria.list());
		return pagination;
	}

	public PageBean findPageBean(final Page<T> page, final String hql, final Object... values) {
		Query q = createQuery(hql, values);
		long totalCount = countHqlResult(hql, values);
		page.setTotalCount(totalCount);
		setPageParameter(q, page);
		// 把分页信息保存到Bean中
		PageBean pageBean = new PageBean();
		pageBean.setPageSize(page.getPageSize());
		pageBean.setCurrentPage(page.getPageNo());
		pageBean.setAllRow(page.getTotalCount().intValue());
		pageBean.setTotalPage(page.getTotalPages().intValue());
		pageBean.setList(page.getData());
		pageBean.init();
		return pageBean;
	}

	/**
	 * 按HQL分页查询.
	 * 
	 * @param page
	 *            分页参数.
	 * @param hql
	 *            hql语句.
	 * @param values
	 *            命名参数,按名称绑定.
	 * 
	 * @return 分页查询结果, 附带结果列表及所有查询时的参数.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Page<T> findPage(final Page<T> page, final String hql, final Map<String, Object> values) {
		Query q = createQuery(hql, values);
		if (page.isAutoCount()) {
			long totalCount = countHqlResult(hql, values);
			page.setTotalCount(totalCount);
		}
		setPageParameter(q, page);
		List result = q.list();
		page.setData(result);
		return page;
	}

	/**
	 * @param hql
	 * @param values
	 * @return
	 */
	public int updateHql(final String hql, final Object... values) {
		return super.createQuery(hql, values).executeUpdate();
	}

	/**
	 * @param hql
	 * @param values
	 * @return
	 */
	public int deleteHql(final String hql, final Object... values) {
		return super.createQuery(hql, values).executeUpdate();
	}

	/**
	 * 设置分页参数到Query对象,辅助函数.
	 */
	protected Query setPageParameter(final Query q, final Page<T> page) {
		// hibernate的firstResult的序号从0开始
		q.setFirstResult(page.getFirst() - 1);
		q.setMaxResults(page.getPageSize());
		return q;
	}

	/**
	 * 设置分页参数到Criteria对象,辅助函数.
	 */
	protected Criteria setPageParameter(final Criteria c, final Page<T> page) {
		// hibernate的firstResult的序号从0开始
		c.setFirstResult(page.getFirst() - 1);
		c.setMaxResults(page.getPageSize());
		if (page.isOrderBySetted()) {
			String[] orderByArray = StringUtils.split(page.getOrderBy(), ',');
			String[] orderArray = StringUtils.split(page.getOrder(), ',');
			for (int i = 0; i < orderByArray.length; i++) {
				if (Page.ASC.equals(orderArray[i])) {
					c.addOrder(Order.asc(orderByArray[i]));
				} else {
					c.addOrder(Order.desc(orderByArray[i]));
				}
			}
		}
		return c;
	}

	/**
	 * 执行count查询获得本次Hql查询所能获得的对象总数.
	 * 
	 * 本函数只能自动处理简单的hql语句,复杂的hql查询请另行编写count语句查询.
	 */
	protected long countHqlResult(final String hql, final Object... values) {
		Long count = 0L;
		String fromHql = hql;
		// select子句与order by子句会影响count查询,进行简单的排除.
		fromHql = "from " + StringUtils.substringAfter(fromHql, "from");
		fromHql = StringUtils.substringBefore(fromHql, "order by");
		fromHql = StringUtils.substringBefore(fromHql, "group by");
		String countHql = "select count(*) " + fromHql;
		try {
			count = findUnique(countHql, values);
		} catch (Exception e) {
			throw new RuntimeException("hql can't be auto count, hql is:" + countHql, e);
		}
		return count;
	}

	/**
	 * 执行count查询获得本次Hql查询所能获得的对象总数.
	 * 
	 * 本函数只能自动处理简单的hql语句,复杂的hql查询请另行编写count语句查询.
	 */
	protected long countHqlResult(final String hql, final Map<String, Object> values) {
		Long count = 0L;
		String fromHql = hql;
		// select子句与order by子句会影响count查询,进行简单的排除.
		fromHql = "from " + StringUtils.substringAfter(fromHql, "from");
		fromHql = StringUtils.substringBefore(fromHql, "order by");
		fromHql = StringUtils.substringBefore(fromHql, "group by");
		String countHql = "select count(*) " + fromHql;
		try {
			count = findUnique(countHql, values);
		} catch (Exception e) {
			throw new RuntimeException("hql can't be auto count, hql is:" + countHql, e);
		}
		return count;
	}

	/**
	 * 仿spring JdbcTemplate 的 queryForList 方法.
	 * 
	 * @param sql
	 * @param objects
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public List queryForList(final String sql, final Object... objects) {
		Session session = this.getSession();
		SQLQuery sqlQuery = (SQLQuery) session.createSQLQuery(sql)
				.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		if (ArrayUtils.isNotEmpty(objects)) {
			sqlQuery.setParameters(objects, this.findTypes(objects));
		}
		return sqlQuery.list();
	}

	@SuppressWarnings("rawtypes")
	public List queryTop(final String sql, final int topSize, final Object... objects) {
		Session session = this.getSession();
		SQLQuery sqlQuery = (SQLQuery) session.createSQLQuery(sql)
				.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		if (ArrayUtils.isNotEmpty(objects)) {
			sqlQuery.setParameters(objects, this.findTypes(objects));
		}
		sqlQuery.setFirstResult(0);
		sqlQuery.setMaxResults(topSize);
		return sqlQuery.list();
	}

	/**
	 * 仿spring JdbcTemplate 的 queryForList 方法.
	 * 
	 * @param sql
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public List queryForList(final String sql) {
		return this.queryForList(sql, null);
	}

	/**
	 * 仿spring JdbcTemplate 的 queryForInt 方法.
	 * 
	 * @param sql
	 * @param objects
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public int queryForInt(final String sql, final Object... objects) {
		Session session = this.getSession();
		SQLQuery sqlQuery = (SQLQuery) session.createSQLQuery(sql);
		if (ArrayUtils.isNotEmpty(objects)) {
			sqlQuery.setParameters(objects, this.findTypes(objects));
		}
		Object obj = sqlQuery.uniqueResult();
		String rsValue = "";
		if (obj instanceof Map) {
			Map result = (Map) obj;
			for (Iterator iterator = result.keySet().iterator(); iterator.hasNext();) {
				Object key = (Object) iterator.next();
				rsValue = ObjectUtils.toString(result.get(key));
			}
		} else {
			rsValue = ObjectUtils.toString(obj);
		}
		return NumberUtils.toInt(rsValue);
	}

	/**
	 * 仿spring JdbcTemplate 的 queryForInt 方法.
	 * 
	 * @param sql
	 * @return
	 */
	public int queryForInt(final String sql) {
		return this.queryForInt(sql, null);
	}

	/**
	 * 仿spring JdbcTemplate 的 queryForLong 方法.
	 * 
	 * @param sql
	 * @param objects
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public long queryForLong(final String sql, final Object... objects) {
		Session session = this.getSession();
		SQLQuery sqlQuery = (SQLQuery) session.createSQLQuery(sql);
		if (ArrayUtils.isNotEmpty(objects)) {
			sqlQuery.setParameters(objects, this.findTypes(objects));
		}
		Object obj = sqlQuery.uniqueResult();
		String rsValue = "";
		if (obj instanceof Map) {
			Map result = (Map) obj;
			for (Iterator iterator = result.keySet().iterator(); iterator.hasNext();) {
				Object key = (Object) iterator.next();
				rsValue = ObjectUtils.toString(result.get(key));
			}
		} else {
			rsValue = ObjectUtils.toString(obj);
		}
		return NumberUtils.toLong(rsValue);
	}

	/**
	 * 仿spring JdbcTemplate 的 queryForLong 方法.
	 * 
	 * @param sql
	 * @param objects
	 * @return
	 */
	public long queryForLong(final String sql) {
		return this.queryForLong(sql, null);
	}

	/**
	 * 仿spring JdbcTemplate 的 update 方法.
	 * 
	 * @param sql
	 * @param objects
	 * @return
	 */
	public int updateSql(final String sql, final Object... objects) {
		Session session = this.getSession();
		SQLQuery sqlQuery = (SQLQuery) session.createSQLQuery(sql);
		if (ArrayUtils.isNotEmpty(objects)) {
			sqlQuery.setParameters(objects, this.findTypes(objects));
		}
		int i = sqlQuery.executeUpdate();
		return i;
	}

	// 拼类型，所有类型都当字符串处理
	protected Type[] findTypes(Object[] objects) {
		List<Type> list = new ArrayList<Type>();
		for (Object object : objects) {
			if (object instanceof Integer) {
				list.add(new IntegerType());
			} else if (object instanceof Long) {
				list.add(new LongType());
			} else if (object instanceof BigDecimal) {
				list.add(new BigDecimalType());
			} else if (object instanceof Character) {
				list.add(new CharacterType());
			} else if (object instanceof Double) {
				list.add(new DoubleType());
			} else if (object instanceof Date) {
				list.add(new DateType());
			} else {
				list.add(new StringType());
			}
		}
		return list.toArray(new Type[] {});
	}

	/**
	 * 批量插入
	 * 
	 * @param iterable
	 * @return
	 */
	public List<?> batchSave(final List<?> list) {
		if (CollectionUtils.isNotEmpty(list)) {
			Session session = getSession();
			for (int i = 0; i < list.size(); i++) {
				session.save(list.get(i));
				if (i % BATCH_SIZE == 0) {
					session.flush();
					session.clear();
				}
			}
		}
		return list;
	}

	/**
	 * 批量插入或更新
	 * 
	 * @param iterable
	 * @return
	 */
	public List<?> batchSaveOrUpdate(final List<?> list) {
		if (CollectionUtils.isNotEmpty(list)) {
			Session session = getSession();
			for (int i = 0; i < list.size(); i++) {
				session.saveOrUpdate(list.get(i));
				if (i % BATCH_SIZE == 0) {
					session.flush();
					session.clear();
				}
			}
		}
		return list;
	}

	/**
	 * 批量更新
	 * 
	 * @param iterable
	 * @return
	 */
	public List<?> batchUpdate(final List<?> list) {
		if (CollectionUtils.isNotEmpty(list)) {
			Session session = getSession();
			for (int i = 0; i < list.size(); i++) {
				session.update(list.get(i));
				if (i % BATCH_SIZE == 0) {
					session.flush();
					session.clear();
				}
			}
		}
		return list;
	}

	/**
	 * 批量物理删除
	 * 
	 * @param iterable
	 * @return
	 */
	public List<?> batchDelete(final List<?> list) {
		if (CollectionUtils.isNotEmpty(list)) {
			Session session = getSession();
			for (int i = 0; i < list.size(); i++) {
				session.delete(list.get(i));
				if (i % BATCH_SIZE == 0) {
					session.flush();
					session.clear();
				}
			}
		}
		return list;
	}

}
