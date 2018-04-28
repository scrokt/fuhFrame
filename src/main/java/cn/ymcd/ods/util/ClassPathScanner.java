package cn.ymcd.ods.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

/**
 * 类路径扫描资源工具,需要spring支持
 * 
 * @author fuh 2015年11月23日
 */
public class ClassPathScanner {

	private static ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	private static MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(
			resourcePatternResolver);

	private static final Logger LOG = Logger.getLogger(ClassPathScanner.class);

	/**
	 * spring资源路径扫描的表达式 <br/>
	 * 例子： <code>classpath*:cn.ymcd.**.*Action</code>
	 * 
	 * @param pattern
	 * @return
	 */
	public static Set<Class<?>> scanClasses(String pattern) {
		HashSet<Class<?>> hashSet = new HashSet<Class<?>>();
		try {
			String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
					+ pattern.replace(".", "/") + ".class";
			Resource[] resources = resourcePatternResolver
					.getResources(packageSearchPath);

			for (Resource re : resources) {
				if (re.isReadable()) {
					MetadataReader metadataReader = metadataReaderFactory
							.getMetadataReader(re);
					ClassMetadata classMetadata = metadataReader
							.getClassMetadata();
					LOG.debug("scan class:" + classMetadata.getClassName());
					Class<?> forName = Class.forName(classMetadata
							.getClassName());
					hashSet.add(forName);
				}
			}
			return hashSet;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return hashSet;
	}

	/**
	 * spring资源路径扫描的表达式 <br/>
	 * 例子： <code>classpath*:cn.ymcd.**.rsf-*</code>
	 * 
	 * @param pattern
	 * @return
	 */
	public static Set<InputStream> scanXml(String pattern) {
		HashSet<InputStream> hashSet = new HashSet<InputStream>();
		try {
			String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
					+ pattern.replace(".", "/") + ".xml";
			Resource[] resources = resourcePatternResolver
					.getResources(packageSearchPath);
			for (Resource re : resources) {
				if (re.isReadable()) {
					hashSet.add(re.getInputStream());
				}
			}
			return hashSet;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return hashSet;
	}
	
	public static Set<InputStream> scanProperties(String pattern) {
        HashSet<InputStream> hashSet = new HashSet<InputStream>();
        try {
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                    + pattern.replace(".", "/") + ".properties";
            Resource[] resources = resourcePatternResolver
                    .getResources(packageSearchPath);
            for (Resource re : resources) {
                if (re.isReadable()) {
                    hashSet.add(re.getInputStream());
                }
            }
            return hashSet;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hashSet;
    }
}
