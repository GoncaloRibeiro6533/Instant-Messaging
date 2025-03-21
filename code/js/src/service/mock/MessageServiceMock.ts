import { MessageService } from '../interfaces/MessageService';
import { Repo } from '../../App';
import { Message } from '../../domain/Message';
import { delay } from './utils';
import { tokenHandler } from './tokenHandler';

export class MessageServiceMock implements MessageService {
    repo: Repo;

  constructor(repo: Repo) {
    this.repo = repo;
  }

        async sendMessage(channelId: number, content: string): Promise<Message> {
            await delay(500)
            const token = tokenHandler().getToken();
            if(!token) throw new Error("Invalid token");
            if(!this.repo.userRepo.getUserByToken(token)) throw new Error("Invalid token");
            const user = this.repo.userRepo.getUserByToken(token)
            if (!user) {
                throw new Error("Invalid token");
            }
            const date = new Date();
            const ch = this.repo.channelRepo.channels.find(channel => channel.id === channelId);
            if (!ch) {
                throw new Error("Channel not found");
            }
            const channelsOfUser = Array.from(this.repo.channelRepo.getChannelsOfUser(user, user.id).keys());
            if (!channelsOfUser.find(channel => channel.id === ch.id)) {
                throw new Error("User is not a member of this channel");
            }
            const role = this.repo.channelRepo.getChannelMembers(user, ch.id).find(member => member.user.id === user.id)!.role;
            if (role === "READ_ONLY") {
                throw new Error("User does not have permission to send messages in this channel");
            }
            const message = this.repo.messageRepo.createMessage(user, ch, content, date);
            return Promise.resolve(message);

        }
    
        async getMessages(channelId: number, limit: number, skip: number): Promise<Message[]> {
            await delay(500)
            const token = tokenHandler().getToken();
            if(!token) throw new Error("Invalid token");
            if(!this.repo.userRepo.getUserByToken(token)) throw new Error("Invalid token");
            const user = this.repo.userRepo.getUserByToken(token)
            if (!user) {
                throw new Error("Invalid token");
            }
            const ch = this.repo.channelRepo.channels.find(channel => channel.id === channelId);
            if (!ch) {
                throw new Error("Channel not found");
            }
            const channelsOfUser = Array.from(this.repo.channelRepo.getChannelsOfUser(user, user.id).keys());
            if (!channelsOfUser.find((channel: { id: number; }) => channel.id === ch.id)) {
                throw new Error("User is not a member of this channel");
            }
            return this.repo.messageRepo.getMessages(ch, limit, skip);
    }
}