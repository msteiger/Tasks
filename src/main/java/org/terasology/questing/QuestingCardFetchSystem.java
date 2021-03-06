/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.questing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.events.ReceivedItemEvent;
import org.terasology.logic.manager.GUIManager;
import org.terasology.questing.gui.UIScreenQuest;
import org.terasology.questing.utils.ModIcons;

@RegisterSystem
public class QuestingCardFetchSystem implements ComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(QuestingCardFetchSystem.class);

    public static String questName = null;
    private static String goal = null;
    public static String friendlyGoal = null;
    private static String amount = null;
    private static Integer currentAmount = 1;

    @Override
    public void initialise() {
        ModIcons.loadIcons();
    }

    @Override
    public void shutdown() {
        // nothing to do
    }

    @ReceiveEvent(components = { InventoryComponent.class }, priority = EventPriority.PRIORITY_HIGH)
    public void onReceiveItem(ReceivedItemEvent event, EntityRef entity) {
        ItemComponent item = event.getItem().getComponent(ItemComponent.class);

        // Make sure we have a valid item
        if (item == null) {
            logger.warn("Got an invalid item for entity {}", entity);
            return;
        }

        String stackID = item.stackId;
        // logger.info("Picked up item with id " + stackID);

        if (goal != null) {
            if (stackID.equals(goal)) {
                Integer amounts = Integer.parseInt(amount);

                if (!currentAmount.equals(amounts)) {
                    currentAmount += 1;
                    logger.info("You have gotten " + currentAmount + " blocks.");
                } else {
                    resetQuest();
                    logger.info("Quest finished! Quest goal is now {}", friendlyGoal);

                    UIScreenQuest.qName.setText("Quest finished!");
                    UIScreenQuest.qGoal.setText(" ");

                    CoreRegistry.get(GUIManager.class).openWindow("journal");
                }
            }
        }
    }

    @ReceiveEvent(components = { QuestingCardFetchComponent.class })
    public void onActivate(ActivateEvent event, EntityRef entity) {
        QuestingCardFetchComponent questingCard = entity.getComponent(QuestingCardFetchComponent.class);

        questName = questingCard.questName;
        goal = questingCard.goal;
        friendlyGoal = questingCard.friendlyGoal;
        amount = questingCard.amount;

        logger.info("Quest is now active! The quest is {}", questName);
    }

    public static void resetQuest() {
        questName = null;
        goal = null;
        friendlyGoal = null;
        amount = null;
        currentAmount = 1;
    }
}
