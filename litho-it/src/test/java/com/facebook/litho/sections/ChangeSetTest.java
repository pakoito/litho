/*
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.sections;

import static com.facebook.litho.sections.Change.MOVE;
import static com.facebook.litho.sections.ChangeSet.acquireChangeSet;
import static org.assertj.core.api.Java6Assertions.assertThat;

import com.facebook.litho.testing.testrunner.ComponentsTestRunner;
import com.facebook.litho.widget.ComponentRenderInfo;
import com.facebook.litho.widget.RenderInfo;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests {@link ChangeSet} */
@RunWith(ComponentsTestRunner.class)
public class ChangeSetTest {

  @Test
  public void testAddChange() {
    final ChangeSet changeSet = ChangeSet.acquireChangeSet(null);

    changeSet.addChange(Change.insert(0, ComponentRenderInfo.createEmpty()));
    assertThat(changeSet.getCount()).isEqualTo(1);

    changeSet.addChange(Change.remove(0));
    assertThat(changeSet.getCount()).isEqualTo(0);

    changeSet.addChange(Change.insert(0, ComponentRenderInfo.createEmpty()));
    assertThat(changeSet.getCount()).isEqualTo(1);
    changeSet.addChange(Change.update(0, ComponentRenderInfo.createEmpty()));
    assertThat(changeSet.getCount()).isEqualTo(1);

    changeSet.addChange(Change.insert(0, ComponentRenderInfo.createEmpty()));
    assertThat(changeSet.getCount()).isEqualTo(2);
    changeSet.addChange(Change.move(0, 1));
    assertThat(changeSet.getCount()).isEqualTo(2);
  }

  @Test
  public void testRangedChange() throws Exception {
    final ChangeSet changeSet = ChangeSet.acquireChangeSet(null);

    changeSet.addChange(Change.insertRange(0, 10, dummyComponentInfos(10)));
    assertThat(changeSet.getCount()).isEqualTo(10);

    changeSet.addChange(Change.removeRange(4, 4));
    assertThat(changeSet.getCount()).isEqualTo(6);

    changeSet.addChange(Change.removeRange(0, 6));
    assertThat(changeSet.getCount()).isEqualTo(0);

    changeSet.addChange(Change.insertRange(0, 8, dummyComponentInfos(8)));
    changeSet.addChange(Change.insertRange(0, 3, dummyComponentInfos(3)));
    assertThat(changeSet.getCount()).isEqualTo(11);

    changeSet.addChange(Change.updateRange(7, 3, dummyComponentInfos(3)));
    assertThat(changeSet.getCount()).isEqualTo(11);

    changeSet.move(9,1);
    assertThat(changeSet.getCount()).isEqualTo(11);
  }

  private List<RenderInfo> dummyComponentInfos(int count) {
    ArrayList<RenderInfo> renderInfos = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      renderInfos.add(ComponentRenderInfo.createEmpty());
    }
    return renderInfos;
  }

  @Test
  public void testInitialCount() {
    assertThat(acquireChangeSet(10, null).getCount()).isEqualTo(10);
    assertThat(acquireChangeSet(null).getCount()).isEqualTo(0);
  }

  @Test
  public void testMerge() {
    final ChangeSet changeSet = ChangeSet.acquireChangeSet(null);
    changeSet.addChange(Change.insert(0, ComponentRenderInfo.createEmpty()));
    changeSet.addChange(Change.insert(1, ComponentRenderInfo.createEmpty()));
    changeSet.addChange(Change.insert(2, ComponentRenderInfo.createEmpty()));

    final ChangeSet secondChangeSet = ChangeSet.acquireChangeSet(null);
    secondChangeSet.addChange(Change.insert(0, ComponentRenderInfo.createEmpty()));
    secondChangeSet.addChange(Change.insert(1, ComponentRenderInfo.createEmpty()));
    secondChangeSet.addChange(Change.insert(2, ComponentRenderInfo.createEmpty()));
    secondChangeSet.addChange(Change.move(0, 1));

    final ChangeSet mergedChangeSet = ChangeSet.merge(changeSet, secondChangeSet);

    assertThat(changeSet.getCount()).isEqualTo(3);
    assertThat(secondChangeSet.getCount()).isEqualTo(3);

    for (int i = 0; i < 3; i++) {
      assertThat(changeSet.getChangeAt(i).getIndex()).isEqualTo(i);
    }

    for (int i = 0; i < 3; i++) {
      assertThat(secondChangeSet.getChangeAt(i).getIndex()).isEqualTo(i);
    }

    assertThat(mergedChangeSet.getCount()).isEqualTo(6);
    assertThat(mergedChangeSet.getChangeCount()).isEqualTo(7);

    for (int i = 0; i < 6; i++) {
      assertThat(mergedChangeSet.getChangeAt(i).getIndex()).isEqualTo(i);
    }

    assertThat(mergedChangeSet.getChangeAt(6).getType()).isEqualTo(MOVE);
    assertThat(mergedChangeSet.getChangeAt(6).getIndex()).isEqualTo(3);
    assertThat(mergedChangeSet.getChangeAt(6).getToIndex()).isEqualTo(4);
  }

  @Test
  public void testRelease() {
    final ChangeSet changeSet = ChangeSet.acquireChangeSet(null);
    changeSet.addChange(Change.insert(0, ComponentRenderInfo.createEmpty()));
    changeSet.addChange(Change.insert(1, ComponentRenderInfo.createEmpty()));
    changeSet.addChange(Change.insert(2, ComponentRenderInfo.createEmpty()));

    changeSet.release();
    assertThat(changeSet.getCount()).isEqualTo(0);
    assertThat(changeSet.getChangeCount()).isEqualTo(0);
  }
}
