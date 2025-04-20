/*
 * Copyright 2020-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.relational.core.dialect;

import java.util.Collection;
import java.util.Collections;

import org.springframework.data.relational.core.sql.LockOptions;
import org.springframework.data.relational.core.sql.SqlIdentifier;

/**
 * An SQL dialect for DB2.
 *
 * @author Jens Schauder
 * @author Mikhail Polivakha
 * @since 2.0
 */
public class Db2Dialect extends AbstractDialect {

	/**
	 * Singleton instance.
	 *
	 * @deprecated use the {@code org.springframework.data.jdbc.core.dialect.JdbcDb2Dialect} directly.
	 */
	@Deprecated(forRemoval = true)
	public static final Db2Dialect INSTANCE = new Db2Dialect();

	private static final IdGeneration ID_GENERATION = new IdGeneration() {
		@Override
		public boolean supportedForBatchOperations() {
			return false;
		}

		@Override
		public String createSequenceQuery(SqlIdentifier sequenceName) {

			/*
			 * This workaround (non-ANSI SQL way of querying sequence) exists for the same reasons it exists for {@link HsqlDbDialect}
			 *
			 * @see HsqlDbDialect#getIdGeneration()#nextValueFromSequenceSelect(String)
			 */
			return "SELECT NEXT VALUE FOR %s FROM SYSCAT.SEQUENCES LIMIT 1"
					.formatted(sequenceName.toSql(INSTANCE.getIdentifierProcessing()));
		}
	};

	protected Db2Dialect() {}

	@Override
	public IdGeneration getIdGeneration() {
		return ID_GENERATION;
	}

	private static final LimitClause LIMIT_CLAUSE = new LimitClause() {

		@Override
		public String getLimit(long limit) {
			return "FETCH FIRST " + limit + " ROWS ONLY";
		}

		@Override
		public String getOffset(long offset) {
			return "OFFSET " + offset + " ROWS";
		}

		@Override
		public String getLimitOffset(long limit, long offset) {
			return String.format("OFFSET %d ROWS FETCH FIRST %d ROWS ONLY", offset, limit);
		}

		@Override
		public Position getClausePosition() {
			return Position.AFTER_ORDER_BY;
		}
	};

	@Override
	public LimitClause limit() {
		return LIMIT_CLAUSE;
	}

	@Override
	public LockClause lock() {

		return new LockClause() {

			@Override
			public String getLock(LockOptions lockOptions) {
				return "FOR UPDATE WITH RS USE AND KEEP EXCLUSIVE LOCKS";
			}

			@Override
			public Position getClausePosition() {
				return Position.AFTER_ORDER_BY;
			}
		};
	}

	@Override
	public Collection<Object> getConverters() {
		return Collections.singletonList(TimestampAtUtcToOffsetDateTimeConverter.INSTANCE);
	}
}
