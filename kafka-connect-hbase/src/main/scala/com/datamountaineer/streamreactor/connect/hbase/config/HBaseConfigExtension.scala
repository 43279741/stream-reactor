/*
 * Copyright 2017-2023 Lenses.io Ltd
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
package com.datamountaineer.streamreactor.connect.hbase.config

import com.datamountaineer.streamreactor.connect.hbase.kerberos.Kerberos
import com.datamountaineer.streamreactor.connect.hbase.kerberos.KeytabSettings
import com.datamountaineer.streamreactor.connect.hbase.kerberos.UserPasswordSettings
import org.apache.hadoop.conf.Configuration

object HBaseConfigExtension {

  implicit class ConfigurationExtension(val configuration: Configuration) extends AnyVal {

    def withKerberos(kerberos: Kerberos): Unit = {
      configuration.set("hadoop.security.authentication", "kerberos")
      kerberos.auth match {
        case Left(keytab)   => withKeyTab(keytab)
        case Right(userPwd) => withUserPassword(userPwd)
      }
    }

    def withUserPassword(settings: UserPasswordSettings): Unit = {
      System.setProperty("java.security.auth.login.config", settings.jaasPath)
      System.setProperty("java.security.krb5.conf", settings.krb5Path)
      System.setProperty("javax.security.auth.useSubjectCredsOnly", "false")
      settings.nameNodePrincipal.foreach(configuration.set("dfs.namenode.kerberos.principal", _))
    }

    def withKeyTab(settings: KeytabSettings): Unit = {
      configuration.set("hadoop.security.authorization", "true")
      settings.nameNodePrincipal.foreach(configuration.set("dfs.namenode.kerberos.principal", _))
    }
  }

}
