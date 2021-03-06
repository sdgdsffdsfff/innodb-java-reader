package com.alibaba.innodb.java.reader.column;

import com.alibaba.innodb.java.reader.AbstractTest;
import com.alibaba.innodb.java.reader.TableReader;
import com.alibaba.innodb.java.reader.page.index.GenericRecord;
import com.alibaba.innodb.java.reader.schema.Column;
import com.alibaba.innodb.java.reader.schema.Schema;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author xu.zx
 */
public class ColumnCharTableReaderTest extends AbstractTest {

  public Schema getSchema() {
    return new Schema().setTableCharset("latin1")
        .addColumn(new Column().setName("id").setType("int(11)").setNullable(false).setPrimaryKey(true))
        .addColumn(new Column().setName("a").setType("varchar(32)").setNullable(false))
        .addColumn(new Column().setName("b").setType("varchar(64)").setNullable(false))
        .addColumn(new Column().setName("c").setType("varchar(255)").setNullable(false))
        .addColumn(new Column().setName("d").setType("varchar(256)").setNullable(false))
        .addColumn(new Column().setName("e").setType("varchar(512)").setNullable(false))
        .addColumn(new Column().setName("f").setType("char(32)").setNullable(false))
        .addColumn(new Column().setName("g").setType("char(255)").setNullable(false));
  }

  @Test
  public void testVarcharColumnMysql56() {
    testVarcharColumn(IBD_FILE_BASE_PATH_MYSQL56 + "column/char/tb04.ibd");
  }

  @Test
  public void testVarcharColumnMysql57() {
    testVarcharColumn(IBD_FILE_BASE_PATH_MYSQL57 + "column/char/tb04.ibd");
  }

  @Test
  public void testVarcharColumnMysql80() {
    testVarcharColumn(IBD_FILE_BASE_PATH_MYSQL80 + "column/char/tb04.ibd");
  }

  public void testVarcharColumn(String path) {
    try (TableReader reader = new TableReader(path, getSchema())) {
      reader.open();

      // check queryByPageNumber
      List<GenericRecord> recordList = reader.queryByPageNumber(3);

      assertThat(recordList.size(), is(10));

      int index = 0;
      for (int i = 1; i <= 10; i++) {
        GenericRecord record = recordList.get(index++);
        Object[] values = record.getValues();
        System.out.println(Arrays.asList(values));

        assertThat(values[0], is(i));

        // if len > 127 && max len <= 255，覆盖这个分支条件
        if ((i % 2) == 0) {
          assertThat(record.get("a"), is(((char) (97 + i)) + StringUtils.repeat('a', 31)));
          assertThat(record.get("b"), is(((char) (97 + i)) + StringUtils.repeat('b', 63)));
          assertThat(record.get("c"), is(((char) (97 + i)) + StringUtils.repeat('c', 254)));
          assertThat(record.get("d"), is(((char) (97 + i)) + StringUtils.repeat('d', 255)));
          assertThat(record.get("e"), is(((char) (97 + i)) + StringUtils.repeat('e', 511)));
          assertThat(record.get("f"), is(((char) (97 + i)) + StringUtils.repeat('f', 31) + StringUtils.repeat(' ', 32 - 31 - 1)));
          assertThat(record.get("g"), is(((char) (97 + i)) + StringUtils.repeat('g', 254) + StringUtils.repeat(' ', 255 - 254 - 1)));
        } else {
          assertThat(record.get("a"), is(((char) (97 + i)) + StringUtils.repeat('a', 8)));
          assertThat(record.get("b"), is(((char) (97 + i)) + StringUtils.repeat('b', 10)));
          assertThat(record.get("c"), is(((char) (97 + i)) + StringUtils.repeat('c', 100)));
          assertThat(record.get("d"), is(((char) (97 + i)) + StringUtils.repeat('d', 126)));
          assertThat(record.get("e"), is(((char) (97 + i)) + StringUtils.repeat('e', 400)));
          assertThat(record.get("f"), is(((char) (97 + i)) + StringUtils.repeat('f', 8) + StringUtils.repeat(' ', 32 - 8 - 1)));
          assertThat(record.get("g"), is(((char) (97 + i)) + StringUtils.repeat('g', 10) + StringUtils.repeat(' ', 255 - 10 - 1)));
        }
      }
    }
  }
}
