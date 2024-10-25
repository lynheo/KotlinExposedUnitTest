package the.lyn.kotlinexposedmockingexample.service

import io.kotest.core.spec.style.StringSpec
import io.mockk.mockk
import net.bytebuddy.ByteBuddy
import net.bytebuddy.asm.Advice
import net.bytebuddy.asm.Advice.Origin
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.implementation.bind.annotation.RuntimeType
import net.bytebuddy.matcher.ElementMatchers
import org.jetbrains.exposed.dao.id.EntityID
import the.lyn.kotlinexposedmockingexample.entity.User
import the.lyn.kotlinexposedmockingexample.service.ExposedWeavingUnitTestHelper.getPOKOInstanceFromExposedEntity
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter

object ExposedWeavingUnitTestHelper {
    val loadedClassMap = mutableMapOf<KClass<*>, Class<*>>()

    fun <T : Any> getPOKOInstanceFromExposedEntity(klass: KClass<T>): T {
        val loadedClass = loadedClassMap[klass]
        if (loadedClass != null) {
            return createAndReturn(loadedClass) as T
        }

        val dbConnectedPropertyList = User::class.memberProperties
            .asSequence()
            .filter { it.visibility == KVisibility.PUBLIC }
            .filter { it.javaField == null }
            .filter { it.javaGetter != null }
            .filter { it is KMutableProperty1 }


        var dynamicTypeBuilder = ByteBuddy()
            .subclass(klass.java)
        dynamicTypeBuilder = dynamicTypeBuilder
            .method(ElementMatchers.any())
            .intercept(MethodDelegation.to(CustomInterceptor::class.java))

        /*
                dbConnectedPropertyList.forEach {
                    dynamicTypeBuilder = dynamicTypeBuilder
                        .method(ElementMatchers.named(it.javaGetter!!.name))
                        .intercept(
                            MethodDelegation.to(
                                ExposedWeavingUnitTestHelper::class.java,
                                ExposedWeavingUnitTestHelper::interceptGetter.name
                            )
                        )
                        .method(ElementMatchers.named((it as KMutableProperty1).javaSetter!!.name))
                        .intercept(
                            MethodDelegation.to(
                                ExposedWeavingUnitTestHelper::class.java,
                                ExposedWeavingUnitTestHelper::interceptSetter.name
                            )
                        )
                }
        */

        val newLoadedClass = dynamicTypeBuilder
            .make()
            .load(klass.java.classLoader)
            .loaded
        loadedClassMap[klass] = newLoadedClass

        return createAndReturn(newLoadedClass) as T
    }

    fun <T : Any> createAndReturn(klass: Class<T>): T {
        val entityId = EntityID<Long>(0L, mockk())
        return klass.getDeclaredConstructor(EntityID::class.java).newInstance(entityId)
    }

    @JvmStatic
    fun interceptGetter(@Origin method: Method): Any? {
        when (method.returnType) {
            Int::class.java -> {
                return 0
            }

            String::class.java -> {
                return ""
            }
        }

        return ""

        println("Getter method ${method.name}")
    }

    @JvmStatic
    @RuntimeType
    fun interceptSetter(@Origin method: Method, @Advice.Argument(0) value: Any) {
        println("Setter method ${method.name}")
    }
}

object CustomInterceptor {
    @JvmStatic
    fun interceptAll(@Origin method: Method): Any? {
        return method.invoke(this)
    }
}

class UserServiceWithWeavingTest : StringSpec({
    "foo" {
        val user = getPOKOInstanceFromExposedEntity(User::class)
        user.userName = "asd";
    }
})